package com.campusos.auth_service.entity;

import com.campusos.auth_service.enums.RoleType;
import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 190)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    private UUID schoolId;

    private UUID teacherId;

    private UUID parentId;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Boolean accountLocked = false;

    private LocalDateTime lastLoginAt;

    public RoleType getRoleType() {
        return role.getName();
    }
}