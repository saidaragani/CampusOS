package com.campusos.school_service.security;

import java.util.UUID;

/**
 * The caller's identity, resolved from the gateway's trusted headers and stored
 * as the Spring Security principal. {@code schoolId} is null for SUPER_ADMIN.
 */
public record AuthenticatedUser(
        UUID userId,
        String email,
        String role,
        UUID schoolId
) {}
