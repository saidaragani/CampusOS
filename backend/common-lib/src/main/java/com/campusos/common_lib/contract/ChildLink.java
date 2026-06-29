package com.campusos.common_lib.contract;

import java.util.UUID;

/**
 * A parent's claimed child, owned by auth-service (parent_student_link). Used by
 * other services to authorize parent access to a student and to resolve the
 * student's school + admission number.
 */
public record ChildLink(
        UUID studentId,
        UUID schoolId,
        String admissionNo
) {}
