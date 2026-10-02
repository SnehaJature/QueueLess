package com.queueless.userservice.dto.response;

import com.queueless.userservice.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
}
