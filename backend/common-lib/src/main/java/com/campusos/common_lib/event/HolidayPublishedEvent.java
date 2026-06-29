package com.campusos.common_lib.event;

import java.time.LocalDate;
import java.util.UUID;

/** Published by calendar-service when a holiday is added. */
public record HolidayPublishedEvent(
        UUID schoolId,
        String name,
        LocalDate fromDate,
        LocalDate toDate
) {}
