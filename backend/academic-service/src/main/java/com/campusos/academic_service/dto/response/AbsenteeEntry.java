package com.campusos.academic_service.dto.response;

import com.campusos.academic_service.enums.AttendanceSession;

import java.util.UUID;

public record AbsenteeEntry(
        UUID studentId,
        String studentName,
        String classLabel,
        AttendanceSession session
) {}
