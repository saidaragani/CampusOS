package com.campusos.school_service.dto.response;

import java.util.UUID;

/** Internal teacher contact projection for notifications (no salary). */
public record TeacherContactDto(
        UUID teacherId,
        UUID schoolId,
        String fullName,
        String email,
        String phone
) {}
