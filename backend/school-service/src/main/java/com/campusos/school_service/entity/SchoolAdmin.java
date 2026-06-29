package com.campusos.school_service.entity;

import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "school_admin",
        uniqueConstraints = @UniqueConstraint(name = "uk_admin_school", columnNames = "school_id"),
        indexes = @Index(name = "idx_sa_user", columnList = "user_id")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolAdmin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** One admin per school (unique). */
    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    /** The auth-service user_account id for this admin's login. */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 20)
    private String phone;
}
