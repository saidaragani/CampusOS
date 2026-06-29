package com.campusos.notification_service.client.dto;

import java.util.UUID;

/** Mirrors school-service StudentContactDto JSON for the birthday scan. */
public record BirthdayStudent(
        UUID studentId,
        UUID schoolId,
        String admissionNo,
        String fullName,
        String classLabel,
        String guardianPhone
) {}
