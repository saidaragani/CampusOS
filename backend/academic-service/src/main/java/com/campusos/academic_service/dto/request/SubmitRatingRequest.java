package com.campusos.academic_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record SubmitRatingRequest(
        @NotNull UUID studentId,

        @NotBlank
        @Pattern(regexp = "\\d{4}-\\d{2}", message = "month must be in YYYY-MM format")
        String month,

        @Min(1) @Max(5)
        int score,

        String remarks
) {}
