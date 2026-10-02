package com.queueless.queueservice.client;

import com.queueless.queueservice.client.dto.BusinessDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class BusinessClientFallback implements BusinessClient {

    @Override
    public BusinessDto getBusinessById(UUID id) {
        log.warn("Feign fallback: business-service unavailable for id={}", id);
        return null;
    }

    @Override
    public List<BusinessDto> getBusinessesByCategory(String category) {
        log.warn("Feign fallback: business-service unavailable for category={}", category);
        return Collections.emptyList();
    }
}
