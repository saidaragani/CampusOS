package com.campusos.common_lib.event;

import java.math.BigDecimal;
import java.util.UUID;

/** Published by fee-service. kind = RECEIPT (status→paid) or REMINDER (cron). */
public record FeeEvent(
        UUID studentId,
        UUID schoolId,
        String feeType,
        String kind,
        BigDecimal amount
) {}
