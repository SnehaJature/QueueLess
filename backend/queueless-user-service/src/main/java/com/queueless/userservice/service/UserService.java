package com.queueless.userservice.service;

import com.queueless.userservice.dto.request.LoginRequest;
import com.queueless.userservice.dto.request.RegisterRequest;
import com.queueless.userservice.dto.request.UpdateUserRequest;
import com.queueless.userservice.dto.response.AuthResponse;
import com.queueless.userservice.dto.response.UserResponse;
import com.queueless.userservice.entity.User;
import com.queueless.userservice.exception.BadRequestException;
import com.queueless.userservice.exception.ResourceNotFoundException;
import com.queueless.userservice.repository.UserRepository;
import com.queueless.userservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("New user registered: {} with role {}", saved.getEmail(), saved.getRole());
        return toResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new BadRequestException("Account is deactivated");
        }

        String token = jwtTokenProvider.generateToken(
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name()
        );

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public UserResponse getUserById(UUID id) {
        return toResponse(findUser(id));
    }

    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = findUser(id);

        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null) {
            if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        return toResponse(userRepository.save(user));
    }

    private User findUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
