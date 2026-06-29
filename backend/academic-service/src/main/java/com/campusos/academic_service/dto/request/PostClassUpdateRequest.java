package com.campusos.academic_service.dto.request;

import com.campusos.academic_service.enums.ClassUpdateType;
import jakarta.validation.constraints.NotBlank;

public record PostClassUpdateRequest(
        @NotBlank String title,
        @NotBlank String body,
        ClassUpdateType type
) {}
