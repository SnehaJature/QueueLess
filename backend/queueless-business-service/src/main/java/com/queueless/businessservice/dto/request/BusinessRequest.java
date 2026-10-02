package com.queueless.businessservice.dto.request;

import com.queueless.businessservice.entity.BusinessCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BusinessRequest {

    @NotBlank(message = "Business name is required")
    private String name;

    @NotNull(message = "Category is required")
    private BusinessCategory category;

    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    private String phone;

    @NotNull(message = "Average service time is required")
    @Min(value = 1, message = "Average service time must be at least 1 minute")
    private Integer averageServiceTime;
}
