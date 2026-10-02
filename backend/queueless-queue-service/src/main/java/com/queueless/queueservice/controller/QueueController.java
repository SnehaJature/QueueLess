package com.queueless.queueservice.controller;

import com.queueless.queueservice.dto.request.CreateQueueRequest;
import com.queueless.queueservice.dto.response.QueueResponse;
import com.queueless.queueservice.dto.response.QueueStatsResponse;
import com.queueless.queueservice.service.QueueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
@Tag(name = "Queues", description = "Queue lifecycle management")
public class QueueController {

    private final QueueService queueService;

    @PostMapping
    @Operation(summary = "Create a new queue for a business (ADMIN/STAFF)")
    public ResponseEntity<QueueResponse> createQueue(@Valid @RequestBody CreateQueueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(queueService.createQueue(request));
    }

    @GetMapping("/business/{businessId}")
    @Operation(summary = "Get all queues for a business")
    public ResponseEntity<List<QueueResponse>> getByBusiness(@PathVariable UUID businessId) {
        return ResponseEntity.ok(queueService.getQueuesByBusiness(businessId));
    }

    @GetMapping("/{queueId}")
    @Operation(summary = "Get queue details")
    public ResponseEntity<QueueResponse> getQueue(@PathVariable UUID queueId) {
        return ResponseEntity.ok(queueService.getQueueById(queueId));
    }

    @PostMapping("/{queueId}/open")
    @Operation(summary = "Open a queue (STAFF/ADMIN)")
    public ResponseEntity<QueueResponse> openQueue(@PathVariable UUID queueId) {
        return ResponseEntity.ok(queueService.openQueue(queueId));
    }

    @PostMapping("/{queueId}/pause")
    @Operation(summary = "Pause a queue (STAFF/ADMIN)")
    public ResponseEntity<QueueResponse> pauseQueue(@PathVariable UUID queueId) {
        return ResponseEntity.ok(queueService.pauseQueue(queueId));
    }

    @PostMapping("/{queueId}/close")
    @Operation(summary = "Close a queue (STAFF/ADMIN)")
    public ResponseEntity<QueueResponse> closeQueue(@PathVariable UUID queueId) {
        return ResponseEntity.ok(queueService.closeQueue(queueId));
    }

    @GetMapping("/{queueId}/stats")
    @Operation(summary = "Get queue statistics")
    public ResponseEntity<QueueStatsResponse> getStats(@PathVariable UUID queueId) {
        return ResponseEntity.ok(queueService.getStats(queueId));
    }
}
