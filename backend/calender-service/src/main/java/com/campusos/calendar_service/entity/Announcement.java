package com.campusos.calendar_service.entity;

import com.campusos.calendar_service.enums.Audience;
import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "announcement",
        indexes = {
                @Index(name = "idx_ann_school", columnList = "school_id,created_at"),
                @Index(name = "idx_ann_class", columnList = "school_id,class_label")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Announcement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Audience audience;

    /** Set when audience = CLASS. */
    @Column(name = "class_label", length = 15)
    private String classLabel;

    @Column(name = "attachment_key", length = 255)
    private String attachmentKey;

    @Column(name = "posted_by_user_id", nullable = false)
    private UUID postedByUserId;
}
