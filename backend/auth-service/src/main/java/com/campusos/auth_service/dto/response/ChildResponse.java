package com.campusos.auth_service.dto.response;

import java.util.UUID;

public record ChildResponse(
    UUID id,
    String admissionNumber,
    String fullName,
    String className,
    String section
) {}
