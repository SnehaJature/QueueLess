package com.queueless.queueservice.controller;

import com.queueless.queueservice.dto.response.AlternativeQueueResponse;
import com.queueless.queueservice.dto.response.TokenResponse;
import com.queueless.queueservice.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
@Tag(name = "Tokens", description = "Queue token operations for customers and staff")
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/{queueId}/join")
    @Operation(summary = "Customer joins a queue")
    public ResponseEntity<TokenResponse> joinQueue(
            @PathVariable UUID queueId,
            @RequestHeader("X-User-Id") String customerId) {
        return ResponseEntity.ok(tokenService.joinQueue(queueId, UUID.fromString(customerId)));
    }

    @GetMapping("/my-token")
    @Operation(summary = "Get customer's active token")
    public ResponseEntity<TokenResponse> getMyToken(@RequestHeader("X-User-Id") String customerId) {
        return ResponseEntity.ok(tokenService.getMyToken(UUID.fromString(customerId)));
    }

    @GetMapping("/tokens/{tokenId}")
    @Operation(summary = "Get token by ID")
    public ResponseEntity<TokenResponse> getToken(@PathVariable UUID tokenId) {
        return ResponseEntity.ok(tokenService.getTokenById(tokenId));
    }

    @DeleteMapping("/tokens/{tokenId}")
    @Operation(summary = "Customer leaves the queue (cancel token)")
    public ResponseEntity<Void> cancelToken(
            @PathVariable UUID tokenId,
            @RequestHeader("X-User-Id") String customerId) {
        tokenService.cancelToken(tokenId, UUID.fromString(customerId));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{queueId}/next")
    @Operation(summary = "Staff calls the next customer")
    public ResponseEntity<TokenResponse> callNext(@PathVariable UUID queueId) {
        return ResponseEntity.ok(tokenService.callNext(queueId));
    }

    @PostMapping("/tokens/{tokenId}/complete")
    @Operation(summary = "Staff marks token as completed")
    public ResponseEntity<TokenResponse> completeToken(@PathVariable UUID tokenId) {
        return ResponseEntity.ok(tokenService.completeToken(tokenId));
    }

    @PostMapping("/tokens/{tokenId}/skip")
    @Operation(summary = "Staff skips a token")
    public ResponseEntity<TokenResponse> skipToken(@PathVariable UUID tokenId) {
        return ResponseEntity.ok(tokenService.skipToken(tokenId));
    }

    @GetMapping("/{queueId}/waiting")
    @Operation(summary = "Get all waiting tokens in a queue (Staff view)")
    public ResponseEntity<List<TokenResponse>> getWaiting(@PathVariable UUID queueId) {
        return ResponseEntity.ok(tokenService.getWaitingTokens(queueId));
    }

    @GetMapping("/{queueId}/alternatives")
    @Operation(summary = "Find shorter queues in the same business category")
    public ResponseEntity<List<AlternativeQueueResponse>> getAlternatives(@PathVariable UUID queueId) {
        return ResponseEntity.ok(tokenService.findAlternativeQueues(queueId));
    }
}
