package com.campusos.auth_service.dto.response;

import com.campusos.auth_service.entity.Role;

public record UserSummaryDto(
        Long id, String email, String fullName, Role role, Long schoolId
) {}
