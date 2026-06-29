package com.campusos.notification_service.dto.event;

import java.math.BigDecimal;
import java.util.UUID;

/** Mirrors fee-service FeeNotification. kind = RECEIPT | REMINDER. */
public record FeeEvent(
        UUID studentId,
        UUID schoolId,
        String feeType,
        String kind,
        BigDecimal amount
) {}
