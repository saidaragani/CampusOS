package com.campusos.fee_service.dto.response;

import com.campusos.fee_service.enums.FeeStatus;
import com.campusos.fee_service.enums.FeeType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record StudentFeeResponse(
        UUID id,
        UUID schoolId,
        UUID studentId,
        String classLabel,
        String academicYear,
        FeeType feeType,
        BigDecimal amount,
        LocalDate dueDate,
        FeeStatus status,
        LocalDate paidOn,
        BigDecimal paidAmount,
        String paymentNote
) {}
