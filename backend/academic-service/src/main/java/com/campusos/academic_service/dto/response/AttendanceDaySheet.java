package com.campusos.academic_service.dto.response;

import com.campusos.academic_service.enums.AttendanceSession;

import java.time.LocalDate;
import java.util.List;

public record AttendanceDaySheet(
        String classLabel,
        LocalDate date,
        AttendanceSession session,
        List<AttendanceEntry> entries
) {}
