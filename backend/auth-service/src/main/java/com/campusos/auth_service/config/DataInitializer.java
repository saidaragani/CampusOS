package com.campusos.auth_service.config;

import com.campusos.auth_service.entity.Role;
import com.campusos.auth_service.enums.RoleType;
import com.campusos.auth_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds the {@code roles} table with the canonical roles. Idempotent: safe to
 * run on every startup.
 *
 * NOTE: the SUPER_ADMIN user is intentionally NOT seeded. Insert it into the
 * {@code user_accounts} table manually, referencing the SUPER_ADMIN row in the
 * {@code roles} table (which this initializer guarantees exists).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
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
}
