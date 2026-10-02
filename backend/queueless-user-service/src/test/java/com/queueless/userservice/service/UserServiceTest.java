package com.queueless.userservice.service;

import com.queueless.userservice.dto.request.LoginRequest;
import com.queueless.userservice.dto.request.RegisterRequest;
import com.queueless.userservice.dto.response.AuthResponse;
import com.queueless.userservice.dto.response.UserResponse;
import com.queueless.userservice.entity.Role;
import com.queueless.userservice.entity.User;
import com.queueless.userservice.exception.BadRequestException;
import com.queueless.userservice.repository.UserRepository;
import com.queueless.userservice.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserService userService;

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Priya Sharma");
        req.setEmail("priya@example.com");
        req.setPassword("secret123");
        req.setRole(Role.CUSTOMER);

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(req.getPassword())).thenReturn("hashed");

        User saved = User.builder().id(UUID.randomUUID())
                .name(req.getName()).email(req.getEmail())
                .password("hashed").role(Role.CUSTOMER).active(true).build();
        when(userRepository.save(any())).thenReturn(saved);

        UserResponse response = userService.register(req);

        assertThat(response.getEmail()).isEqualTo("priya@example.com");
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void register_duplicateEmail_throwsBadRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("priya@example.com");
        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void login_success_returnsToken() {
        LoginRequest req = new LoginRequest();
        req.setEmail("priya@example.com");
        req.setPassword("secret123");

        User user = User.builder().id(UUID.randomUUID())
                .email(req.getEmail()).password("hashed")
                .name("Priya").role(Role.CUSTOMER).active(true).build();

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(req.getPassword(), "hashed")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any(), any(), any())).thenReturn("jwt-token");

        AuthResponse response = userService.login(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void login_wrongPassword_throwsBadRequest() {
        LoginRequest req = new LoginRequest();
        req.setEmail("priya@example.com");
        req.setPassword("wrong");

        User user = User.builder().email(req.getEmail()).password("hashed")
                .active(true).build();
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.login(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_userNotFound_throwsBadRequest() {
        LoginRequest req = new LoginRequest();
        req.setEmail("ghost@example.com");
        req.setPassword("pass");
        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(req))
                .isInstanceOf(BadRequestException.class);
    }
}
