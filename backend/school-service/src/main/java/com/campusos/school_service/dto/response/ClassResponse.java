package com.campusos.school_service.dto.response;

import java.util.UUID;

public record ClassResponse(
        UUID id,
        UUID schoolId,
        String classLabel,
        UUID teacherId,
        String teacherName,
        String academicYear
) {}
