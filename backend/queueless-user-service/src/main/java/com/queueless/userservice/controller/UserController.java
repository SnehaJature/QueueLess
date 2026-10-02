package com.queueless.userservice.controller;

import com.queueless.userservice.dto.request.LoginRequest;
import com.queueless.userservice.dto.request.RegisterRequest;
import com.queueless.userservice.dto.request.UpdateUserRequest;
import com.queueless.userservice.dto.response.AuthResponse;
import com.queueless.userservice.dto.response.UserResponse;
import com.queueless.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User registration, login, and profile management")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile from gateway header")
    public ResponseEntity<UserResponse> getProfile(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(userService.getUserById(UUID.fromString(userId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}
