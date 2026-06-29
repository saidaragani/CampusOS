package com.campusos.common_lib.contract;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Aggregate fee collection for a school. Produced by fee-service; consumed by
 * school-service for cross-school reports.
 */
public record SchoolFeeStats(
        UUID schoolId,
        BigDecimal paidTotal,
        BigDecimal pendingTotal,
        long paidCount,
        long pendingCount,
        double collectionPercentage
) {}
