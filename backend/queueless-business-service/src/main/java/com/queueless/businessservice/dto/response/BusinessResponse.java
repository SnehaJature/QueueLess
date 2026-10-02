package com.queueless.businessservice.dto.response;

import com.queueless.businessservice.entity.BusinessCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class BusinessResponse {
    private UUID id;
    private String name;
    private BusinessCategory category;
    private String description;
    private String address;
    private String city;
    private String phone;
    private Integer averageServiceTime;
    private boolean open;
    private LocalDateTime createdAt;
}
