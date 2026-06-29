package com.campusos.school_service.entity;

import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "teacher",
        indexes = @Index(name = "idx_teacher_school", columnList = "school_id")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Teacher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    /** The auth-service user_account id for this teacher's login. */
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 190)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 150)
    private String qualification;

    /** Admin-only visibility — never exposed to teacher/parent responses. */
    @Column(precision = 10, scale = 2)
    private BigDecimal salary;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}
