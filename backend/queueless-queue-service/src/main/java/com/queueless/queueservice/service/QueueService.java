package com.queueless.queueservice.service;

import com.queueless.queueservice.client.BusinessClient;
import com.queueless.queueservice.client.dto.BusinessDto;
import com.queueless.queueservice.dto.request.CreateQueueRequest;
import com.queueless.queueservice.dto.response.QueueResponse;
import com.queueless.queueservice.dto.response.QueueStatsResponse;
import com.queueless.queueservice.entity.Queue;
import com.queueless.queueservice.entity.QueueStatus;
import com.queueless.queueservice.entity.TokenStatus;
import com.queueless.queueservice.exception.BadRequestException;
import com.queueless.queueservice.exception.ResourceNotFoundException;
import com.queueless.queueservice.repository.QueueRepository;
import com.queueless.queueservice.repository.QueueTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueueService {

    private final QueueRepository queueRepository;
    private final QueueTokenRepository tokenRepository;
    private final BusinessClient businessClient;

    public QueueResponse createQueue(CreateQueueRequest request) {
        Queue queue = Queue.builder()
                .businessId(request.getBusinessId())
                .queueName(request.getQueueName())
                .status(QueueStatus.CLOSED)
                .currentTokenNumber(0)
                .averageServiceTime(request.getAverageServiceTime())
                .build();
        Queue saved = queueRepository.save(queue);
        log.info("Queue created for business {}: {}", request.getBusinessId(), saved.getId());
        return toResponse(saved, resolveBusinessName(saved.getBusinessId()));
    }

    public List<QueueResponse> getQueuesByBusiness(UUID businessId) {
        String businessName = resolveBusinessName(businessId);
        return queueRepository.findByBusinessId(businessId).stream()
                .map(q -> toResponse(q, businessName))
                .collect(Collectors.toList());
    }

    public QueueResponse getQueueById(UUID queueId) {
        Queue queue = findQueue(queueId);
        return toResponse(queue, resolveBusinessName(queue.getBusinessId()));
    }

    public QueueResponse openQueue(UUID queueId) {
        Queue queue = findQueue(queueId);
        if (queue.getStatus() == QueueStatus.OPEN) {
            throw new BadRequestException("Queue is already open");
        }
        queue.setStatus(QueueStatus.OPEN);
        queue.setOpenedAt(LocalDateTime.now());
        queue.setClosedAt(null);
        log.info("Queue opened: {}", queueId);
        return toResponse(queueRepository.save(queue), resolveBusinessName(queue.getBusinessId()));
    }

    public QueueResponse pauseQueue(UUID queueId) {
        Queue queue = findQueue(queueId);
        if (queue.getStatus() != QueueStatus.OPEN) {
            throw new BadRequestException("Only an open queue can be paused");
        }
        queue.setStatus(QueueStatus.PAUSED);
        log.info("Queue paused: {}", queueId);
        return toResponse(queueRepository.save(queue), resolveBusinessName(queue.getBusinessId()));
    }

    public QueueResponse closeQueue(UUID queueId) {
        Queue queue = findQueue(queueId);
        if (queue.getStatus() == QueueStatus.CLOSED) {
            throw new BadRequestException("Queue is already closed");
        }
        queue.setStatus(QueueStatus.CLOSED);
        queue.setClosedAt(LocalDateTime.now());
        log.info("Queue closed: {}", queueId);
        return toResponse(queueRepository.save(queue), resolveBusinessName(queue.getBusinessId()));
    }

    public QueueStatsResponse getStats(UUID queueId) {
        findQueue(queueId);
        return QueueStatsResponse.builder()
                .totalServed(tokenRepository.countByQueueIdAndStatus(queueId, TokenStatus.COMPLETED))
                .totalSkipped(tokenRepository.countByQueueIdAndStatus(queueId, TokenStatus.SKIPPED))
                .currentlyWaiting(tokenRepository.countByQueueIdAndStatus(queueId, TokenStatus.WAITING))
                .averageServiceTime(findQueue(queueId).getAverageServiceTime())
                .build();
    }

    public Queue findQueue(UUID queueId) {
        return queueRepository.findById(queueId)
                .orElseThrow(() -> new ResourceNotFoundException("Queue not found: " + queueId));
    }

    private String resolveBusinessName(UUID businessId) {
        try {
            BusinessDto dto = businessClient.getBusinessById(businessId);
            return dto != null ? dto.getName() : "Unknown Business";
        } catch (Exception e) {
            log.warn("Could not fetch business name for {}: {}", businessId, e.getMessage());
            return "Unknown Business";
        }
    }

    public QueueResponse toResponse(Queue q, String businessName) {
        long waiting = tokenRepository.countByQueueIdAndStatus(q.getId(), TokenStatus.WAITING);
        return QueueResponse.builder()
                .id(q.getId())
                .businessId(q.getBusinessId())
                .businessName(businessName)
                .queueName(q.getQueueName())
                .status(q.getStatus())
                .currentTokenNumber(q.getCurrentTokenNumber())
                .averageServiceTime(q.getAverageServiceTime())
                .waitingCount(waiting)
                .openedAt(q.getOpenedAt())
                .closedAt(q.getClosedAt())
                .build();
    }
}
