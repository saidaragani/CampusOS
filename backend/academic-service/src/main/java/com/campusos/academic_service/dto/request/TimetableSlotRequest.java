package com.campusos.academic_service.dto.request;

import com.campusos.academic_service.enums.Weekday;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.UUID;

public record TimetableSlotRequest(
        @NotNull Weekday dayOfWeek,
        int periodNo,
        @NotBlank String subject,
        LocalTime startTime,
        LocalTime endTime,
        UUID teacherId
) {}
