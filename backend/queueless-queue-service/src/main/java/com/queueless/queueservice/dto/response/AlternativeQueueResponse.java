package com.queueless.queueservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class AlternativeQueueResponse {
    private UUID businessId;
    private String businessName;
    private String city;
    private UUID queueId;
    private long waitingCount;
    private int estimatedWaitMinutes;
}
