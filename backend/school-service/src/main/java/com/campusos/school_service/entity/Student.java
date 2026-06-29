package com.campusos.school_service.entity;

import com.campusos.common_lib.entity.BaseEntity;
import com.campusos.school_service.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "student",
        uniqueConstraints = @UniqueConstraint(name = "uk_school_admission", columnNames = {"school_id", "admission_no"}),
        indexes = {
                @Index(name = "idx_student_class", columnList = "school_id,class_label"),
                @Index(name = "idx_student_school", columnList = "school_id"),
                @Index(name = "idx_student_dob", columnList = "date_of_birth")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    /** Auto-generated, e.g. "GVS-2025-0001". Unique within a school. */
    @Column(name = "admission_no", nullable = false, length = 40)
    private String admissionNo;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /** "6-A" — set at admission, the single source of truth for class membership. */
    @Column(name = "class_label", nullable = false, length = 15)
    private String classLabel;

    @Column(name = "father_name", length = 150)
    private String fatherName;

    @Column(name = "mother_name", length = 150)
    private String motherName;

    @Column(name = "guardian_phone", length = 20)
    private String guardianPhone;

    @Column(length = 255)
    private String address;

    @Column(length = 100)
    private String village;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Builder.Default
    @Column(name = "has_bus", nullable = false)
    private Boolean hasBus = false;

    @Column(name = "bus_pickup_point", length = 150)
    private String busPickupPoint;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "admission_date")
    private LocalDate admissionDate;
}
