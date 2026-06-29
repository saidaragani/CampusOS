package com.campusos.fee_service.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Generate student_fee rows for every student in a class for the given year. */
public record GenerateFeesRequest(
        @NotBlank String academicYear,
        @NotBlank String classLabel
) {}
