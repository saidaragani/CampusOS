package com.campusos.auth_service.config;

import com.campusos.auth_service.entity.Role;
import com.campusos.auth_service.entity.User;
import com.campusos.auth_service.enums.RoleType;
import com.campusos.auth_service.repository.RoleRepository;
import com.campusos.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        final String email = "saidaragani@campusos.com";

        if (userRepository.existsByEmail(email)) {
            log.info("Super Admin already exists.");
            return;
        }

        Role superAdminRole = roleRepository.findByName(RoleType.SUPER_ADMIN)
                .orElseThrow(() ->
                        new RuntimeException("SUPER_ADMIN role not found in roles table."));

        User superAdmin = User.builder()
                .fullName("Super Admin")
                .email(email)
                .password(passwordEncoder.encode("ChangeMe@123"))
                .enabled(true)
                .accountLocked(false)
                .role(superAdminRole)
                .build();

        userRepository.save(superAdmin);

        log.info("======================================");
        log.info("SUPER ADMIN CREATED");
        log.info("Email    : {}", email);
        log.info("Password : ChangeMe@123");
        log.info("======================================");
    }
}