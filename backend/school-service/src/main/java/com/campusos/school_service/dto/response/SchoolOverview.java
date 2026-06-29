package com.campusos.school_service.dto.response;

import java.util.UUID;

public record SchoolOverview(
        UUID schoolId,
        String name,
        String code,
        long studentCount,
        long teacherCount,
        long classCount
) {}
