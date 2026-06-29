package com.campusos.academic_service.dto.request;

import com.campusos.academic_service.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record CorrectAttendanceRequest(
        @NotNull AttendanceStatus status
) {}
