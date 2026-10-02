package com.queueless.queueservice.service;

import com.queueless.queueservice.entity.TokenStatus;
import com.queueless.queueservice.repository.QueueTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WaitTimeCalculatorTest {

    @Mock
    private QueueTokenRepository tokenRepository;

    @InjectMocks
    private WaitTimeCalculator calculator;

    @Test
    void countPeopleAhead_returnsCorrectCount() {
        UUID queueId = UUID.randomUUID();
        when(tokenRepository.countByQueueIdAndStatusAndTokenNumberLessThan(queueId, TokenStatus.WAITING, 27))
                .thenReturn(5L);

        long result = calculator.countPeopleAhead(queueId, 27);

        assertThat(result).isEqualTo(5);
    }

    @Test
    void estimatedWaitMinutes_multipliesPeopleAheadByServiceTime() {
        UUID queueId = UUID.randomUUID();
        when(tokenRepository.countByQueueIdAndStatusAndTokenNumberLessThan(queueId, TokenStatus.WAITING, 27))
                .thenReturn(5L);

        int result = calculator.estimatedWaitMinutes(queueId, 27, 10);

        assertThat(result).isEqualTo(50); // 5 people × 10 min
    }

    @Test
    void estimatedWaitMinutes_whenNoOneAhead_returnsZero() {
        UUID queueId = UUID.randomUUID();
        when(tokenRepository.countByQueueIdAndStatusAndTokenNumberLessThan(queueId, TokenStatus.WAITING, 1))
                .thenReturn(0L);

        int result = calculator.estimatedWaitMinutes(queueId, 1, 15);

        assertThat(result).isEqualTo(0);
    }
}
