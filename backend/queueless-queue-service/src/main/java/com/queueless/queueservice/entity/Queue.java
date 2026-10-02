package com.queueless.queueservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "queues", indexes = {
    @Index(name = "idx_queue_business", columnList = "businessId")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Queue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID businessId;

    @Column(nullable = false)
    private String queueName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status;

    @Column(nullable = false)
    private Integer currentTokenNumber;

    @Column(nullable = false)
    private Integer averageServiceTime; // minutes

    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
}
