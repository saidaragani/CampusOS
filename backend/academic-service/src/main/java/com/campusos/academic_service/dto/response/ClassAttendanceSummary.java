package com.campusos.academic_service.dto.response;

public record ClassAttendanceSummary(
        String classLabel,
        long present,
        long absent,
        long leave,
        long total,
        double presentPercentage
) {}
