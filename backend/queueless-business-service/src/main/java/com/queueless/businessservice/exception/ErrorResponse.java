package com.queueless.businessservice.exception;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data @Builder
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private Map<String, String> validationErrors;
}
