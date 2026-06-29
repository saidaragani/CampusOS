package com.campusos.school_service.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

/** Admin-facing teacher view — includes salary. Never returned to teacher/parent. */
public record TeacherResponse(
        UUID id,
        UUID schoolId,
        UUID userId,
        String fullName,
        String email,
        String phone,
        String qualification,
        BigDecimal salary,
        Boolean active
) {}
