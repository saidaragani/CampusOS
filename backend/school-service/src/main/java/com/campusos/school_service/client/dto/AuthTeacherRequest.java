package com.campusos.school_service.client.dto;

import java.util.UUID;

/** Mirrors auth-service CreateTeacherRequest JSON. */
public record AuthTeacherRequest(
        String fullName,
        String email,
        String phone,
        String password,
        UUID teacherId
) {}
