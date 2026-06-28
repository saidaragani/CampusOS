package com.campusos.auth_service.dto.response;

import com.campusos.auth_service.enums.RoleType;
import java.util.UUID;

public record UserResponse(
    UUID id,
    UUID schoolId,
    String fullName,
    String email,
    String phone,
    RoleType role
) {}
