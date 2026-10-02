package com.queueless.businessservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceRequest {

    @NotBlank(message = "Service name is required")
    private String name;

    private String description;

    @NotNull(message = "Estimated minutes is required")
    @Min(value = 1, message = "Estimated minutes must be at least 1")
    private Integer estimatedMinutes;
}
