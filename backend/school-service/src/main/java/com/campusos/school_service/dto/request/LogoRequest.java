package com.campusos.school_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoRequest(
        @NotBlank(message = "logoUrl is required")
        String logoUrl
) {}
