package com.queueless.queueservice.repository;

import com.queueless.queueservice.entity.QueueToken;
import com.queueless.queueservice.entity.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueueTokenRepository extends JpaRepository<QueueToken, UUID> {

    List<QueueToken> findByQueueIdAndStatusOrderByTokenNumberAsc(UUID queueId, TokenStatus status);

    Optional<QueueToken> findByQueueIdAndCustomerIdAndStatusIn(UUID queueId, UUID customerId, List<TokenStatus> statuses);

    Optional<QueueToken> findByCustomerIdAndStatusIn(UUID customerId, List<TokenStatus> statuses);

    List<QueueToken> findByQueueIdOrderByTokenNumberAsc(UUID queueId);

    long countByQueueIdAndStatus(UUID queueId, TokenStatus status);

    long countByQueueIdAndStatusIn(UUID queueId, List<TokenStatus> statuses);

    // Count tokens with tokenNumber less than given value and still waiting
    long countByQueueIdAndStatusAndTokenNumberLessThan(UUID queueId, TokenStatus status, Integer tokenNumber);
}
