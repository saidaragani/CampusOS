package com.campusos.school_service.service;

import java.util.UUID;

public interface AdmissionNumberService {

    /**
     * Generates the next per-school admission number, e.g. {@code GVS-2025-0001}.
     */
    String generate(UUID schoolId, String schoolCode, int year);
}
