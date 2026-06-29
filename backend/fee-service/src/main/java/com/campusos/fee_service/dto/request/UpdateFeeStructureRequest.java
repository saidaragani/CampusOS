package com.campusos.fee_service.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateFeeStructureRequest(
        BigDecimal schoolFeeAmount,
        BigDecimal busFeeAmount,
        LocalDate dueDate
) {}
