package com.campusos.school_service.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Cross-school report row for the super admin. Counts always present; the
 * attendance/fee metrics are null when academic/fee-service is unavailable
 * (best-effort enrichment).
 */
public record SchoolReport(
        UUID schoolId,
        String name,
        String code,
        long studentCount,
        long teacherCount,
        long classCount,
        Double attendancePercentage,
        Double feeCollectionPercentage,
        BigDecimal feePaidTotal,
        BigDecimal feePendingTotal
) {}
