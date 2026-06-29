package com.campusos.school_service.entity;

import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * The teacher-to-class-label mapping. This row IS the "class" — there is no
 * separate class entity. One class teacher per (school, class_label).
 */
@Entity
@Table(
        name = "class_teacher",
        uniqueConstraints = @UniqueConstraint(name = "uk_school_class", columnNames = {"school_id", "class_label"}),
        indexes = @Index(name = "idx_ct_teacher", columnList = "teacher_id")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassTeacher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    /** e.g. "6-A", "10-C". */
    @Column(name = "class_label", nullable = false, length = 15)
    private String classLabel;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;

    @Column(name = "academic_year", length = 9)
    private String academicYear;
}
