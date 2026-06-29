package com.campusos.notification_service.entity;

import com.campusos.common_lib.entity.BaseEntity;
import com.campusos.notification_service.enums.Channel;
import com.campusos.notification_service.enums.NotificationStatus;
import com.campusos.notification_service.enums.NotificationTemplate;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "notification_log",
        indexes = {
                @Index(name = "idx_nl_school", columnList = "school_id,created_at"),
                @Index(name = "idx_nl_status", columnList = "status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "school_id")
    private UUID schoolId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 10)
    private Channel channel = Channel.EMAIL;

    @Column(name = "recipient_email", nullable = false, length = 190)
    private String recipientEmail;

    @Column(nullable = false, length = 255)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationTemplate template;

    @Column(name = "related_routing_key", length = 60)
    private String relatedRoutingKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NotificationStatus status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
