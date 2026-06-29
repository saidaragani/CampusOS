package com.campusos.calendar_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record HolidayRequest(
        @NotBlank String name,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        String description
) {}
