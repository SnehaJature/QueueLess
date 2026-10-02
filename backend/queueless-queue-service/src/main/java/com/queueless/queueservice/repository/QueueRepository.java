package com.queueless.queueservice.repository;

import com.queueless.queueservice.entity.Queue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueueRepository extends JpaRepository<Queue, UUID> {
    List<Queue> findByBusinessId(UUID businessId);
    Optional<Queue> findByBusinessIdAndStatusNot(UUID businessId, com.queueless.queueservice.entity.QueueStatus status);
}
