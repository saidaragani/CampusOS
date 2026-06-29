package com.campusos.school_service.client.dto;

import java.util.UUID;

/** Mirrors auth-service UserSummaryDto JSON. */
public record AuthUserResponse(
        UUID id,
        String email,
        String role,
        UUID schoolId
) {}
