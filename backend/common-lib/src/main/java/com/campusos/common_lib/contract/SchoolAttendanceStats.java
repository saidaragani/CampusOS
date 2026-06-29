package com.campusos.common_lib.contract;

import java.util.UUID;

/**
 * Aggregate attendance for a school over a date range. Produced by
 * academic-service; consumed by school-service for cross-school reports.
 */
public record SchoolAttendanceStats(
        UUID schoolId,
        long present,
        long absent,
        long leave,
        long total,
        double presentPercentage
) {}
