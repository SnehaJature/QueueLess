package com.queueless.queueservice.service;

import com.queueless.queueservice.client.BusinessClient;
import com.queueless.queueservice.client.dto.BusinessDto;
import com.queueless.queueservice.entity.Queue;
import com.queueless.queueservice.entity.QueueStatus;
import com.queueless.queueservice.entity.QueueToken;
import com.queueless.queueservice.entity.TokenStatus;
import com.queueless.queueservice.exception.AlreadyJoinedQueueException;
import com.queueless.queueservice.exception.BadRequestException;
import com.queueless.queueservice.exception.QueueClosedException;
import com.queueless.queueservice.repository.QueueRepository;
import com.queueless.queueservice.repository.QueueTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock private QueueTokenRepository tokenRepository;
    @Mock private QueueRepository queueRepository;
    @Mock private QueueService queueService;
    @Mock private WaitTimeCalculator waitTimeCalculator;
    @Mock private BusinessClient businessClient;

    @InjectMocks
    private TokenService tokenService;

    private UUID queueId;
    private UUID customerId;
    private Queue openQueue;

    @BeforeEach
    void setUp() {
        queueId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        openQueue = Queue.builder()
                .id(queueId)
                .businessId(UUID.randomUUID())
                .queueName("General Consultation")
                .status(QueueStatus.OPEN)
                .currentTokenNumber(5)
                .averageServiceTime(10)
                .build();
    }

    @Test
    void joinQueue_success_assignsNextToken() {
        when(queueService.findQueue(queueId)).thenReturn(openQueue);
        when(tokenRepository.findByQueueIdAndCustomerIdAndStatusIn(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(queueRepository.save(any())).thenReturn(openQueue);

        QueueToken savedToken = QueueToken.builder()
                .id(UUID.randomUUID()).queueId(queueId).customerId(customerId)
                .tokenNumber(6).status(TokenStatus.WAITING).build();
        when(tokenRepository.save(any())).thenReturn(savedToken);
        when(waitTimeCalculator.countPeopleAhead(any(), any())).thenReturn(0L);
        when(waitTimeCalculator.estimatedWaitMinutes(any(), any(), anyInt())).thenReturn(0);
        when(businessClient.getBusinessById(any())).thenReturn(new BusinessDto());

        var response = tokenService.joinQueue(queueId, customerId);

        assertThat(response.getTokenNumber()).isEqualTo(6);
        assertThat(response.getStatus()).isEqualTo(TokenStatus.WAITING);
    }

    @Test
    void joinQueue_closedQueue_throwsQueueClosedException() {
        openQueue.setStatus(QueueStatus.CLOSED);
        when(queueService.findQueue(queueId)).thenReturn(openQueue);

        assertThatThrownBy(() -> tokenService.joinQueue(queueId, customerId))
                .isInstanceOf(QueueClosedException.class);
    }

    @Test
    void joinQueue_pausedQueue_throwsQueueClosedException() {
        openQueue.setStatus(QueueStatus.PAUSED);
        when(queueService.findQueue(queueId)).thenReturn(openQueue);

        assertThatThrownBy(() -> tokenService.joinQueue(queueId, customerId))
                .isInstanceOf(QueueClosedException.class);
    }

    @Test
    void joinQueue_alreadyInQueue_throwsAlreadyJoinedQueueException() {
        when(queueService.findQueue(queueId)).thenReturn(openQueue);
        when(tokenRepository.findByQueueIdAndCustomerIdAndStatusIn(any(), any(), any()))
                .thenReturn(Optional.of(QueueToken.builder().tokenNumber(3).build()));

        assertThatThrownBy(() -> tokenService.joinQueue(queueId, customerId))
                .isInstanceOf(AlreadyJoinedQueueException.class);
    }

    @Test
    void completeToken_alreadyCompleted_throwsBadRequest() {
        UUID tokenId = UUID.randomUUID();
        QueueToken token = QueueToken.builder().id(tokenId).status(TokenStatus.COMPLETED).build();
        when(tokenRepository.findById(tokenId)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> tokenService.completeToken(tokenId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already completed");
    }

    @Test
    void skipToken_completedToken_throwsBadRequest() {
        UUID tokenId = UUID.randomUUID();
        QueueToken token = QueueToken.builder().id(tokenId).status(TokenStatus.COMPLETED).build();
        when(tokenRepository.findById(tokenId)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> tokenService.skipToken(tokenId))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void callNext_noWaitingCustomers_throwsBadRequest() {
        when(queueService.findQueue(queueId)).thenReturn(openQueue);
        when(tokenRepository.findByQueueIdAndStatusOrderByTokenNumberAsc(queueId, TokenStatus.WAITING))
                .thenReturn(List.of());

        assertThatThrownBy(() -> tokenService.callNext(queueId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No customers waiting");
    }

    @Test
    void cancelToken_wrongCustomer_throwsBadRequest() {
        UUID tokenId = UUID.randomUUID();
        QueueToken token = QueueToken.builder()
                .id(tokenId).customerId(UUID.randomUUID()).status(TokenStatus.WAITING).build();
        when(tokenRepository.findById(tokenId)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> tokenService.cancelToken(tokenId, customerId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("your own token");
    }
}
