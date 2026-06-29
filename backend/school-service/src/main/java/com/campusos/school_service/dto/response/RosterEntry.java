package com.campusos.school_service.dto.response;

import java.util.UUID;

/** A teacher's roster row — student + guardian contact, no admin-only fields. */
public record RosterEntry(
        UUID studentId,
        String admissionNo,
        String fullName,
        String guardianPhone,
        String classLabel
) {}
