package com.campusos.school_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateClassRequest(
        @NotBlank(message = "Class label is required")
        String classLabel,

        @NotNull(message = "Class teacher is required")
        UUID teacherId,

        String academicYear
) {}
