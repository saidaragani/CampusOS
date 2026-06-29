package com.campusos.notification_service.dto.event;

import java.time.LocalDate;
import java.util.UUID;

/** Mirrors calendar-service HolidayNotification. */
public record HolidayEvent(
        UUID schoolId,
        String name,
        LocalDate fromDate,
        LocalDate toDate
) {}
