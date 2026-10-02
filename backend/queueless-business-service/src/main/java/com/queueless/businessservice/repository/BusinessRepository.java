package com.queueless.businessservice.repository;

import com.queueless.businessservice.entity.Business;
import com.queueless.businessservice.entity.BusinessCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
    List<Business> findByCategory(BusinessCategory category);
    List<Business> findByCityIgnoreCase(String city);
    List<Business> findByOpen(boolean open);
    List<Business> findByCategoryAndCityIgnoreCase(BusinessCategory category, String city);
    List<Business> findByCategoryAndOpen(BusinessCategory category, boolean open);
    List<Business> findByCityIgnoreCaseAndOpen(String city, boolean open);
    List<Business> findByCategoryAndCityIgnoreCaseAndOpen(BusinessCategory category, String city, boolean open);
}
