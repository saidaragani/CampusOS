package com.campusos.school_service.dto.response;

import java.util.UUID;

/** Lightweight internal projection for academic/fee/messaging services. */
public record StudentContactDto(
        UUID studentId,
        UUID schoolId,
        String admissionNo,
        String fullName,
        String classLabel,
        String guardianPhone
) {}
