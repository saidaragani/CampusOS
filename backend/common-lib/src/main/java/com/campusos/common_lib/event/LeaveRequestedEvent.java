package com.campusos.common_lib.event;

import java.time.LocalDate;
import java.util.UUID;

/** Published by academic-service when a parent applies for leave (notifies the class teacher). */
public record LeaveRequestedEvent(
        UUID leaveId,
        UUID studentId,
        UUID schoolId,
        String classLabel,
        LocalDate fromDate,
        LocalDate toDate,
        String reason
) {}
