package com.campusos.fee_service.dto.response;

import java.math.BigDecimal;

public record FeeSummaryResponse(
        long paidCount,
        BigDecimal paidTotal,
        long pendingCount,
        BigDecimal pendingTotal
) {}
