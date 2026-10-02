package com.queueless.userservice.config;

import com.queueless.userservice.entity.Role;
import com.queueless.userservice.entity.User;
import com.queueless.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createDefaultUser("System Admin", "admin@gmail.com",  "admin123",    Role.ADMIN);
        createDefaultUser("Staff User",   "staff@gmail.com",  "staff123",    Role.STAFF);
        createDefaultUser("Test Customer","customer@gmail.com","customer123", Role.CUSTOMER);
    }

    private void createDefaultUser(String name, String email, String password, Role role) {
        if (!userRepository.existsByEmail(email)) {
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(role)
                    .active(true)
                    .build();
            userRepository.save(user);
            log.info("Default {} created: {}", role, email);
        }
    }
}
