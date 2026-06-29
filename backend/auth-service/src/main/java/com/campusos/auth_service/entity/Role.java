package com.campusos.auth_service.entity;

import com.campusos.auth_service.enums.RoleType;
import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Role is a first-class persisted entity (table {@code roles}) so that new roles
 * can be introduced in the database without code changes. The {@link RoleType}
 * enum names the roles the application seeds and references in code.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 40)
    private RoleType name;

    @Column(length = 200)
    private String description;
}
