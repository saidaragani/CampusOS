package com.campusos.school_service.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PhotoRequest(
        @NotBlank(message = "photoUrl is required")
        String photoUrl
) {}
