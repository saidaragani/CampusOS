package com.campusos.academic_service.dto.response;

import java.util.List;
import java.util.UUID;

/** A student's attendance for a month: each session mark + the monthly totals. */
public record StudentAttendanceView(
        UUID studentId,
        String month,
        List<AttendanceDayMark> daily,
        long present,
        long absent,
        long leave,
        long total,
        double presentPercentage
) {}
