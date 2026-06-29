package com.campusos.calendar_service.client.dto;

import java.time.LocalDate;
import java.util.UUID;

public record HolidayNotification(
        UUID schoolId,
        String name,
        LocalDate fromDate,
        LocalDate toDate
) {}
