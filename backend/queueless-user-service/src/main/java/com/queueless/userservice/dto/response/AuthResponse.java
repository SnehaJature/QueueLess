package com.queueless.userservice.dto.response;

import com.queueless.userservice.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data @Builder
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private UUID userId;
    private String name;
    private String email;
    private Role role;
}
