package com.campusos.academic_service.dto.request;

import com.campusos.academic_service.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AttendanceMark(
        @NotNull UUID studentId,
        @NotNull AttendanceStatus status
) {}
