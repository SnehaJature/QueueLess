package com.queueless.businessservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class ServiceResponse {
    private UUID id;
    private UUID businessId;
    private String name;
    private String description;
    private Integer estimatedMinutes;
    private boolean active;
}
