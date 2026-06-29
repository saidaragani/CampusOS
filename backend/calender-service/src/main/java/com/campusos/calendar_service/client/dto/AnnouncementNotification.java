package com.campusos.calendar_service.client.dto;

import java.util.UUID;

public record AnnouncementNotification(
        UUID schoolId,
        String audience,
        String classLabel,
        String title,
        String body
) {}
