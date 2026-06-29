package com.campusos.notification_service.dto.event;

import java.time.LocalDate;
import java.util.UUID;

/** Mirrors academic-service AbsenteeNotification. */
public record AbsenteeEvent(
        UUID studentId,
        UUID schoolId,
        String classLabel,
        LocalDate date,
        String session
) {}
