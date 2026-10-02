package com.queueless.queueservice.dto.response;

import com.queueless.queueservice.entity.QueueStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class QueueResponse {
    private UUID id;
    private UUID businessId;
    private String businessName;
    private String queueName;
    private QueueStatus status;
    private Integer currentTokenNumber;
    private Integer averageServiceTime;
    private long waitingCount;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
}
