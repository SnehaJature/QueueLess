package com.queueless.queueservice.client.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class BusinessDto {
    private UUID id;
    private String name;
    private String category;
    private String city;
    private Integer averageServiceTime;
    private boolean open;
}
