package com.queueless.queueservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateQueueRequest {

    @NotNull(message = "Business ID is required")
    private UUID businessId;

    @NotBlank(message = "Queue name is required")
    private String queueName;

    @NotNull(message = "Average service time is required")
    @Min(value = 1, message = "Average service time must be at least 1 minute")
    private Integer averageServiceTime;
}
