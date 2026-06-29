package com.campusos.academic_service.client.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Emitted when a student is marked absent. message-service resolves the parent's
 * email (via auth) and sends the alert.
 */
public record AbsenteeNotification(
        UUID studentId,
        UUID schoolId,
        String classLabel,
        LocalDate date,
        String session
) {}
