package com.campusos.academic_service.entity;

import com.campusos.academic_service.enums.ClassUpdateType;
import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "class_update",
        indexes = @Index(name = "idx_cu_class", columnList = "school_id,class_label,created_at")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassUpdate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "class_label", nullable = false, length = 15)
    private String classLabel;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 10)
    private ClassUpdateType type = ClassUpdateType.NOTE;

    @Column(name = "posted_by_teacher_id", nullable = false)
    private UUID postedByTeacherId;
}
