package com.campusos.school_service.dto.response;

import java.util.UUID;

public record SchoolAdminResponse(
        UUID id,
        UUID schoolId,
        UUID userId,
        String fullName,
        String phone
) {}
