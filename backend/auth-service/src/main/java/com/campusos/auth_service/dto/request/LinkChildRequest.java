package com.campusos.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Used by an already-registered PARENT to claim an additional child (sibling)
 * by admission number.
 */
public record LinkChildRequest(
        @NotNull(message = "School is required")
        UUID schoolId,

        @NotBlank(message = "Admission number is required")
        String admissionNo
) {}
