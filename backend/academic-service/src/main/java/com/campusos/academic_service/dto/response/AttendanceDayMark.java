package com.campusos.academic_service.dto.response;

import com.campusos.academic_service.enums.AttendanceSession;
import com.campusos.academic_service.enums.AttendanceStatus;

import java.time.LocalDate;

public record AttendanceDayMark(
        LocalDate date,
        AttendanceSession session,
        AttendanceStatus status
) {}
