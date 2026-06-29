package com.campusos.academic_service.dto.response;

import com.campusos.academic_service.enums.AttendanceStatus;

import java.util.UUID;

/** One row of a day sheet. {@code status} is null when the student isn't marked yet. */
public record AttendanceEntry(
        UUID studentId,
        String admissionNo,
        String studentName,
        AttendanceStatus status
) {}
