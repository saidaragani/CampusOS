package com.campusos.auth_service.dto.response;

import java.util.UUID;

public record UserSummaryDto(
        UUID id,
        String email,
        String fullName,
        String role,
        UUID schoolId
) {}
