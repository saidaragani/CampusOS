package com.campusos.auth_service.entity;

import com.campusos.common_lib.entity.BaseEntity;
import com.campusos.auth_service.enums.RoleType;
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

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(length = 15)
    private String phone;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    private UUID schoolId;

    private UUID teacherId;

    private Boolean enabled = true;

    private Boolean accountLocked = false;

    private LocalDateTime lastLoginAt;

    public RoleType getRoleType() {
        return role.getName();
    }
}