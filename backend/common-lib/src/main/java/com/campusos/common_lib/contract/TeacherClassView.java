package com.campusos.common_lib.contract;

import java.util.UUID;

/**
 * A teacher's identity + class assignment, resolved by school-service from the
 * auth user id. {@code classLabel} is null when the teacher isn't assigned a class yet.
 * Consumed by academic-service (and others) to scope teacher actions.
 */
public record TeacherClassView(
        UUID teacherId,
        UUID schoolId,
        String classLabel
) {}
