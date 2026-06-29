package com.campusos.fee_service.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Emitted on a fee status change / reminder. {@code kind} is RECEIPT or REMINDER;
 * message-service resolves the parent email and sends.
 */
public record FeeNotification(
        UUID studentId,
        UUID schoolId,
        String feeType,
        String kind,
        BigDecimal amount
) {}
