package com.campusos.fee_service.dto.request;

import java.math.BigDecimal;

public record MarkPaidRequest(
        BigDecimal paidAmount,
        String paymentNote
) {}
