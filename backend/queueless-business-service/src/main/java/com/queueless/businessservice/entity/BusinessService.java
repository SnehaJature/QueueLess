package com.queueless.businessservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "services")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BusinessService {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID businessId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer estimatedMinutes;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
