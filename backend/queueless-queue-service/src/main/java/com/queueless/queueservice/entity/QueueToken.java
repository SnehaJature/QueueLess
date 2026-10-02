package com.queueless.queueservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "queue_tokens", indexes = {
    @Index(name = "idx_token_queue", columnList = "queueId"),
    @Index(name = "idx_token_customer", columnList = "customerId"),
    @Index(name = "idx_token_status", columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QueueToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID queueId;

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false)
    private Integer tokenNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenStatus status;

    private LocalDateTime joinedAt;
    private LocalDateTime calledAt;
    private LocalDateTime completedAt;
    private LocalDateTime skippedAt;
}
