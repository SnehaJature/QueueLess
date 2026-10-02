package com.queueless.queueservice.dto.response;

import com.queueless.queueservice.entity.TokenStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class TokenResponse {
    private UUID tokenId;
    private UUID queueId;
    private String businessName;
    private String queueName;
    private Integer tokenNumber;
    private Integer currentServingToken;
    private long peopleAhead;
    private int estimatedWaitMinutes;
    private TokenStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime calledAt;
    private LocalDateTime completedAt;
}
