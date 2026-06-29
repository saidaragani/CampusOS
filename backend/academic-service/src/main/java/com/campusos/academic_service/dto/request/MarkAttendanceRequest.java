package com.campusos.academic_service.dto.request;

import com.campusos.academic_service.enums.AttendanceSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** Bulk-mark the teacher's class for a date + session. */
public record MarkAttendanceRequest(
        @NotNull LocalDate date,
        @NotNull AttendanceSession session,
        @NotEmpty @Valid List<AttendanceMark> marks
) {}
