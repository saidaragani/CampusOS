package com.campusos.common_lib.contract;

import java.util.UUID;

/**
 * A student as needed by services that work off a class roster (academic, fee).
 * Produced by school-service for a (schoolId, classLabel). {@code hasBus} lets
 * fee-service decide whether to generate a bus fee.
 */
public record RosterStudent(
        UUID studentId,
        UUID schoolId,
        String admissionNo,
        String fullName,
        String classLabel,
        String guardianPhone,
        boolean hasBus
) {}
