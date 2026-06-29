package com.campusos.common_lib.contract;

import java.util.UUID;

/**
 * Shared inter-service contract describing a student's identity and class
 * context. Owned/produced by student-service (which resolves the class teacher
 * from the school's teacher-to-class mapping). Consumed for admission validation
 * and for the parent portal ("Class: 6-A, Class Teacher: Ramesh").
 *
 * <p>A non-null {@code studentId} signals a valid admission number.
 */
public record StudentSummary(
        UUID studentId,
        UUID schoolId,
        String admissionNo,
        String studentName,
        String classLabel,
        String section,
        String classTeacherName
) {}
