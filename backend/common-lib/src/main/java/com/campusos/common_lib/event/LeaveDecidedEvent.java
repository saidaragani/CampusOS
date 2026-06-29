package com.campusos.common_lib.event;

import java.util.UUID;

/** Published by academic-service when a teacher approves/rejects leave (notifies the parent). */
public record LeaveDecidedEvent(
        UUID leaveId,
        UUID studentId,
        UUID schoolId,
        boolean approved,
        String note
) {}
