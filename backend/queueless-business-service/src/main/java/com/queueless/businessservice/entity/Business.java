package com.queueless.businessservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "businesses", indexes = {
    @Index(name = "idx_business_category", columnList = "category"),
    @Index(name = "idx_business_city", columnList = "city")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessCategory category;

    private String description;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    private String phone;

    @Column(nullable = false)
    private Integer averageServiceTime; // in minutes

    @Builder.Default
    @Column(nullable = false)
    private boolean open = false;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
