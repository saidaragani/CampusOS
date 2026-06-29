package com.campusos.notification_service.dto.event;

import java.util.UUID;

/** Mirrors calendar-service AnnouncementNotification. audience = ALL_PARENTS | CLASS | TEACHERS. */
public record AnnouncementEvent(
        UUID schoolId,
        String audience,
        String classLabel,
        String title,
        String body
) {}
