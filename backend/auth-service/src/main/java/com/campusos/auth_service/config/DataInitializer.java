package com.campusos.auth_service.config;

import com.campusos.auth_service.entity.Role;
import com.campusos.auth_service.entity.User;
import com.campusos.auth_service.enums.RoleType;
import com.campusos.auth_service.repository.RoleRepository;
import com.campusos.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the {@code roles} table with the canonical roles and bootstraps a single
 * SUPER_ADMIN from configuration. Idempotent: safe to run on every startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.super-admin.email}")
    private String superAdminEmail;

    @Value("${app.super-admin.password}")
    private String superAdminPassword;

    @Value("${app.super-admin.full-name:Super Admin}")
    private String superAdminFullName;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedRoles();
        seedSuperAdmin();
    }

    private void seedRoles() {
        for (RoleType type : RoleType.values()) {
            roleRepository.findByName(type).orElseGet(() -> {
                log.info("Seeding role {}", type);
                return roleRepository.save(Role.builder()
                        .name(type)
                        .description(type.name() + " role")
                        .build());
            });
        }
    }

    private void seedSuperAdmin() {
        if (userRepository.existsByEmail(superAdminEmail)) {
            return;
        }
        Role superAdminRole = roleRepository.findByName(RoleType.SUPER_ADMIN)
                .orElseThrow(() -> new IllegalStateException("SUPER_ADMIN role was not seeded"));

        userRepository.save(User.builder()
                .email(superAdminEmail)
                .password(passwordEncoder.encode(superAdminPassword))
                .fullName(superAdminFullName)
                .role(superAdminRole)
                .enabled(true)
                .build());
        log.info("Seeded SUPER_ADMIN account: {}", superAdminEmail);
    }
}
