package com.campusos.notification_service.dto.response;

import com.campusos.notification_service.enums.Channel;
import com.campusos.notification_service.enums.NotificationStatus;
import com.campusos.notification_service.enums.NotificationTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationLogResponse(
        UUID id,
        UUID schoolId,
        Channel channel,
        String recipientEmail,
        String subject,
        NotificationTemplate template,
        NotificationStatus status,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime sentAt
) {}
