package com.campusos.academic_service.dto.response;

import com.campusos.academic_service.enums.LeaveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeaveResponse(
        UUID id,
        UUID studentId,
        String classLabel,
        LocalDate fromDate,
        LocalDate toDate,
        String reason,
        LeaveStatus status,
        String decisionNote,
        LocalDateTime decidedAt
) {}
