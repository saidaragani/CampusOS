package com.campusos.calendar_service.entity;

import com.campusos.calendar_service.enums.MediaType;
import com.campusos.common_lib.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "gallery_item",
        indexes = {
                @Index(name = "idx_gallery_school", columnList = "school_id,created_at"),
                @Index(name = "idx_gallery_event", columnList = "event_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GalleryItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    /** Optional link to a school_event. */
    @Column(name = "event_id")
    private UUID eventId;

    @Column(length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 10)
    private MediaType mediaType;

    /** Object storage key (MinIO later); stored as metadata for now. */
    @Column(name = "object_key", nullable = false, length = 255)
    private String objectKey;

    @Column(name = "thumbnail_key", length = 255)
    private String thumbnailKey;

    @Column(name = "uploaded_by_user_id", nullable = false)
    private UUID uploadedByUserId;
}
