package com.campusos.calendar_service.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record HolidayResponse(
        UUID id,
        UUID schoolId,
        String name,
        LocalDate fromDate,
        LocalDate toDate,
        String description
) {}
