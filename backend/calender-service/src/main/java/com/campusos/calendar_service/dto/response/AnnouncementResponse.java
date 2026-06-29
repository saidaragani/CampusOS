package com.campusos.calendar_service.dto.response;

import com.campusos.calendar_service.enums.Audience;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnnouncementResponse(
        UUID id,
        UUID schoolId,
        String title,
        String body,
        Audience audience,
        String classLabel,
        String attachmentKey,
        UUID postedByUserId,
        LocalDateTime createdAt
) {}
