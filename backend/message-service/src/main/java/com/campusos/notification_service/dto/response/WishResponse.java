package com.campusos.notification_service.dto.response;

import com.campusos.notification_service.enums.WishAudience;

import java.time.LocalDate;
import java.util.UUID;

public record WishResponse(
        UUID id,
        UUID schoolId,
        String festivalName,
        String message,
        LocalDate scheduledDate,
        WishAudience audience,
        boolean sent
) {}
