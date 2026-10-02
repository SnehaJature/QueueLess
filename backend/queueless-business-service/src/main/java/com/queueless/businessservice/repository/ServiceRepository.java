package com.queueless.businessservice.repository;

import com.queueless.businessservice.entity.BusinessService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<BusinessService, UUID> {
    List<BusinessService> findByBusinessIdAndActive(UUID businessId, boolean active);
    List<BusinessService> findByBusinessId(UUID businessId);
}
