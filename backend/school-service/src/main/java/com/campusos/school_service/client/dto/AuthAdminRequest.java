package com.campusos.school_service.client.dto;

import java.util.UUID;

/** Mirrors auth-service CreateAdminRequest JSON. */
public record AuthAdminRequest(
        String fullName,
        String email,
        String phone,
        String password,
        UUID schoolId
) {}
