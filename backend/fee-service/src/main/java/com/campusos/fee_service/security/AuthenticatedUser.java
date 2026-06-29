package com.campusos.fee_service.security;

import java.util.UUID;

/** Caller identity resolved from the gateway headers. schoolId is null for SUPER_ADMIN. */
public record AuthenticatedUser(
        UUID userId,
        String email,
        String role,
        UUID schoolId
) {}
