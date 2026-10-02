package com.queueless.queueservice.service;

import com.queueless.queueservice.client.BusinessClient;
import com.queueless.queueservice.client.dto.BusinessDto;
import com.queueless.queueservice.dto.response.AlternativeQueueResponse;
import com.queueless.queueservice.dto.response.TokenResponse;
import com.queueless.queueservice.entity.Queue;
import com.queueless.queueservice.entity.QueueStatus;
import com.queueless.queueservice.entity.QueueToken;
import com.queueless.queueservice.entity.TokenStatus;
import com.queueless.queueservice.exception.AlreadyJoinedQueueException;
import com.queueless.queueservice.exception.BadRequestException;
import com.queueless.queueservice.exception.QueueClosedException;
import com.queueless.queueservice.exception.ResourceNotFoundException;
import com.queueless.queueservice.repository.QueueRepository;
import com.queueless.queueservice.repository.QueueTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final QueueTokenRepository tokenRepository;
    private final QueueRepository queueRepository;
    private final QueueService queueService;
    private final WaitTimeCalculator waitTimeCalculator;
    private final BusinessClient businessClient;

    @Transactional
    public TokenResponse joinQueue(UUID queueId, UUID customerId) {
        Queue queue = queueService.findQueue(queueId);

        if (queue.getStatus() == QueueStatus.CLOSED) {
            throw new QueueClosedException("This queue is currently closed.");
        }
        if (queue.getStatus() == QueueStatus.PAUSED) {
            throw new QueueClosedException("This queue is currently paused and not accepting new customers.");
        }

        // Prevent duplicate active tokens in the same queue
        tokenRepository.findByQueueIdAndCustomerIdAndStatusIn(
                queueId, customerId, List.of(TokenStatus.WAITING, TokenStatus.CALLED, TokenStatus.SERVING)
        ).ifPresent(t -> {
            throw new AlreadyJoinedQueueException("You are already in this queue with token #" + t.getTokenNumber());
        });

        int nextToken = queue.getCurrentTokenNumber() + 1;
        queue.setCurrentTokenNumber(nextToken);
        queueRepository.save(queue);

        QueueToken token = QueueToken.builder()
                .queueId(queueId)
                .customerId(customerId)
                .tokenNumber(nextToken)
                .status(TokenStatus.WAITING)
                .joinedAt(LocalDateTime.now())
                .build();

        QueueToken saved = tokenRepository.save(token);
        log.info("Customer {} joined queue {} with token #{}", customerId, queueId, nextToken);

        return buildTokenResponse(saved, queue);
    }

    public TokenResponse getMyToken(UUID customerId) {
        QueueToken token = tokenRepository.findByCustomerIdAndStatusIn(
                customerId, List.of(TokenStatus.WAITING, TokenStatus.CALLED, TokenStatus.SERVING)
        ).orElseThrow(() -> new ResourceNotFoundException("No active token found for this customer"));

        Queue queue = queueService.findQueue(token.getQueueId());
        return buildTokenResponse(token, queue);
    }

    public TokenResponse getTokenById(UUID tokenId) {
        QueueToken token = findToken(tokenId);
        Queue queue = queueService.findQueue(token.getQueueId());
        return buildTokenResponse(token, queue);
    }

    @Transactional
    public TokenResponse callNext(UUID queueId) {
        Queue queue = queueService.findQueue(queueId);
        if (queue.getStatus() != QueueStatus.OPEN) {
            throw new BadRequestException("Queue must be open to call next customer");
        }

        QueueToken next = tokenRepository
                .findByQueueIdAndStatusOrderByTokenNumberAsc(queueId, TokenStatus.WAITING)
                .stream().findFirst()
                .orElseThrow(() -> new BadRequestException("No customers waiting in this queue"));

        next.setStatus(TokenStatus.CALLED);
        next.setCalledAt(LocalDateTime.now());
        tokenRepository.save(next);
        log.info("Token #{} called in queue {}", next.getTokenNumber(), queueId);

        return buildTokenResponse(next, queue);
    }

    @Transactional
    public TokenResponse completeToken(UUID tokenId) {
        QueueToken token = findToken(tokenId);
        if (token.getStatus() == TokenStatus.COMPLETED) {
            throw new BadRequestException("Token is already completed");
        }
        if (token.getStatus() == TokenStatus.SKIPPED) {
            throw new BadRequestException("Skipped tokens cannot be completed");
        }
        if (token.getStatus() == TokenStatus.CANCELLED) {
            throw new BadRequestException("Cancelled tokens cannot be completed");
        }

        token.setStatus(TokenStatus.COMPLETED);
        token.setCompletedAt(LocalDateTime.now());
        tokenRepository.save(token);
        log.info("Token #{} completed", token.getTokenNumber());

        Queue queue = queueService.findQueue(token.getQueueId());
        return buildTokenResponse(token, queue);
    }

    @Transactional
    public TokenResponse skipToken(UUID tokenId) {
        QueueToken token = findToken(tokenId);
        if (token.getStatus() == TokenStatus.COMPLETED) {
            throw new BadRequestException("Completed tokens cannot be skipped");
        }
        if (token.getStatus() == TokenStatus.SKIPPED) {
            throw new BadRequestException("Token is already skipped");
        }

        token.setStatus(TokenStatus.SKIPPED);
        token.setSkippedAt(LocalDateTime.now());
        tokenRepository.save(token);
        log.info("Token #{} skipped", token.getTokenNumber());

        Queue queue = queueService.findQueue(token.getQueueId());
        return buildTokenResponse(token, queue);
    }

    @Transactional
    public void cancelToken(UUID tokenId, UUID customerId) {
        QueueToken token = findToken(tokenId);

        if (!token.getCustomerId().equals(customerId)) {
            throw new BadRequestException("You can only cancel your own token");
        }
        if (token.getStatus() == TokenStatus.COMPLETED) {
            throw new BadRequestException("Completed tokens cannot be cancelled");
        }
        if (token.getStatus() == TokenStatus.CANCELLED) {
            throw new BadRequestException("Token is already cancelled");
        }

        token.setStatus(TokenStatus.CANCELLED);
        tokenRepository.save(token);
        log.info("Customer {} cancelled token #{}", customerId, token.getTokenNumber());
    }

    public List<TokenResponse> getWaitingTokens(UUID queueId) {
        Queue queue = queueService.findQueue(queueId);
        return tokenRepository.findByQueueIdAndStatusOrderByTokenNumberAsc(queueId, TokenStatus.WAITING)
                .stream().map(t -> buildTokenResponse(t, queue)).collect(Collectors.toList());
    }

    /**
     * Smart queue recommendation: find other open queues in the same business category
     * with fewer people waiting than the current queue.
     */
    public List<AlternativeQueueResponse> findAlternativeQueues(UUID queueId) {
        Queue currentQueue = queueService.findQueue(queueId);
        long currentWaiting = tokenRepository.countByQueueIdAndStatus(queueId, TokenStatus.WAITING);

        BusinessDto currentBusiness;
        try {
            currentBusiness = businessClient.getBusinessById(currentQueue.getBusinessId());
        } catch (Exception e) {
            log.warn("Could not fetch business info for alternatives: {}", e.getMessage());
            return List.of();
        }

        if (currentBusiness == null) return List.of();

        List<BusinessDto> sameCategory = businessClient.getBusinessesByCategory(currentBusiness.getCategory());

        return sameCategory.stream()
                .filter(b -> !b.getId().equals(currentQueue.getBusinessId()) && b.isOpen())
                .flatMap(b -> queueRepository.findByBusinessId(b.getId()).stream()
                        .filter(q -> q.getStatus() == QueueStatus.OPEN)
                        .map(q -> {
                            long waiting = tokenRepository.countByQueueIdAndStatus(q.getId(), TokenStatus.WAITING);
                            return AlternativeQueueResponse.builder()
                                    .businessId(b.getId())
                                    .businessName(b.getName())
                                    .city(b.getCity())
                                    .queueId(q.getId())
                                    .waitingCount(waiting)
                                    .estimatedWaitMinutes((int) (waiting * q.getAverageServiceTime()))
                                    .build();
                        })
                )
                .filter(alt -> alt.getWaitingCount() < currentWaiting)
                .sorted((a, b) -> Long.compare(a.getWaitingCount(), b.getWaitingCount()))
                .collect(Collectors.toList());
    }

    private TokenResponse buildTokenResponse(QueueToken token, Queue queue) {
        long peopleAhead = waitTimeCalculator.countPeopleAhead(queue.getId(), token.getTokenNumber());
        int estimatedWait = waitTimeCalculator.estimatedWaitMinutes(
                queue.getId(), token.getTokenNumber(), queue.getAverageServiceTime());

        String businessName = "Unknown";
        try {
            BusinessDto b = businessClient.getBusinessById(queue.getBusinessId());
            if (b != null) businessName = b.getName();
        } catch (Exception ignored) {}

        return TokenResponse.builder()
                .tokenId(token.getId())
                .queueId(queue.getId())
                .businessName(businessName)
                .queueName(queue.getQueueName())
                .tokenNumber(token.getTokenNumber())
                .currentServingToken(queue.getCurrentTokenNumber())
                .peopleAhead(peopleAhead)
                .estimatedWaitMinutes(estimatedWait)
                .status(token.getStatus())
                .joinedAt(token.getJoinedAt())
                .calledAt(token.getCalledAt())
                .completedAt(token.getCompletedAt())
                .build();
    }

    private QueueToken findToken(UUID tokenId) {
        return tokenRepository.findById(tokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found: " + tokenId));
    }
}
