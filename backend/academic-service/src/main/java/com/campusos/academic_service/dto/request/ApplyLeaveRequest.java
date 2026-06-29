package com.campusos.academic_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ApplyLeaveRequest(
        @NotNull UUID studentId,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        @NotBlank String reason
) {}
