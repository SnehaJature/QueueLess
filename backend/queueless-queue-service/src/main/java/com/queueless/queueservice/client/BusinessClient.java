package com.queueless.queueservice.client;

import com.queueless.queueservice.client.dto.BusinessDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

// name = "business-service" matches the spring.application.name in business-service.
// Eureka resolves this to the actual host:port at runtime — no hardcoded URLs.
@FeignClient(name = "business-service", fallback = BusinessClientFallback.class)
public interface BusinessClient {

    @GetMapping("/api/businesses/{id}")
    BusinessDto getBusinessById(@PathVariable UUID id);

    @GetMapping("/api/businesses")
    List<BusinessDto> getBusinessesByCategory(@RequestParam String category);
}
