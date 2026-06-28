package com.campusos.auth_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LinkChildRequest(
    @NotNull
    Long schoolId,
    @NotBlank
    String admissionNumber
) {
}
