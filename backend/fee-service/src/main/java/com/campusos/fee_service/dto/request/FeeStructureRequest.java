package com.campusos.fee_service.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeStructureRequest(
        @NotBlank String academicYear,
        /** null = applies to all classes. */
        String classLabel,
        BigDecimal schoolFeeAmount,
        BigDecimal busFeeAmount,
        LocalDate dueDate
) {}
