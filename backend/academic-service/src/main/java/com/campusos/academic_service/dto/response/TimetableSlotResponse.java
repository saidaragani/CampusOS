package com.campusos.academic_service.dto.response;

import com.campusos.academic_service.enums.Weekday;

import java.time.LocalTime;
import java.util.UUID;

public record TimetableSlotResponse(
        UUID id,
        Weekday dayOfWeek,
        int periodNo,
        String subject,
        LocalTime startTime,
        LocalTime endTime,
        UUID teacherId
) {}
