package com.campusos.notification_service.entity;

import com.campusos.common_lib.entity.BaseEntity;
import com.campusos.notification_service.enums.WishAudience;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "wish_schedule", indexes = @Index(name = "idx_ws_due", columnList = "sent,scheduled_date"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "festival_name", nullable = false, length = 150)
    private String festivalName;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 15)
    private WishAudience audience = WishAudience.ALL_PARENTS;

    @Builder.Default
    @Column(nullable = false)
    private boolean sent = false;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;
}
