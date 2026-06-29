package com.campusos.common_lib.event;

import java.time.LocalDate;
import java.util.UUID;

/** Published by academic-service when a student is marked absent. */
public record AbsentEvent(
        UUID studentId,
        UUID schoolId,
        String classLabel,
        LocalDate date,
        String session
) {}
