package com.queueless.queueservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class QueueStatsResponse {
    private long totalServed;
    private long totalSkipped;
    private long currentlyWaiting;
    private int averageServiceTime;
}
