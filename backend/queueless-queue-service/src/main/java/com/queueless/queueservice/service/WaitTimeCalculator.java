package com.queueless.queueservice.service;

import com.queueless.queueservice.entity.TokenStatus;
import com.queueless.queueservice.repository.QueueTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Calculates how many people are ahead of a given token and the estimated wait time.
 *
 * peopleAhead = number of WAITING tokens with a lower token number than the customer's token.
 * estimatedWait = peopleAhead × averageServiceTime (minutes)
 */
@Component
@RequiredArgsConstructor
public class WaitTimeCalculator {

    private final QueueTokenRepository tokenRepository;

    public long countPeopleAhead(UUID queueId, Integer tokenNumber) {
        return tokenRepository.countByQueueIdAndStatusAndTokenNumberLessThan(
                queueId, TokenStatus.WAITING, tokenNumber);
    }

    public int estimatedWaitMinutes(UUID queueId, Integer tokenNumber, int averageServiceTime) {
        long ahead = countPeopleAhead(queueId, tokenNumber);
        return (int) (ahead * averageServiceTime);
    }
}
