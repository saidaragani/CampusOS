package com.campusos.fee_service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record FeeStructureResponse(
        UUID id,
        UUID schoolId,
        String academicYear,
        String classLabel,
        BigDecimal schoolFeeAmount,
        BigDecimal busFeeAmount,
        LocalDate dueDate
) {}
