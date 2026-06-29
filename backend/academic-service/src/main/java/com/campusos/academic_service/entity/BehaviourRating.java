package com.campusos.academic_service.entity;

import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "behaviour_rating",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_rating_student_month",
                columnNames = {"student_id", "rating_month"}),
        indexes = @Index(name = "idx_rating_class_month", columnList = "school_id,class_label,rating_month")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviourRating extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "class_label", nullable = false, length = 15)
    private String classLabel;

    /** "2025-06" */
    @Column(name = "rating_month", nullable = false, length = 7)
    private String ratingMonth;

    /** 1..5 */
    @Column(name = "behaviour_score", nullable = false)
    private int behaviourScore;

    @Column(length = 500)
    private String remarks;

    @Column(name = "rated_by_teacher_id", nullable = false)
    private UUID ratedByTeacherId;
}
