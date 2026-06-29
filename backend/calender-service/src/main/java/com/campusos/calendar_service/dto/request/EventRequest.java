package com.campusos.calendar_service.dto.request;

import com.campusos.calendar_service.enums.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EventRequest(
        @NotBlank String title,
        String description,
        @NotNull LocalDate eventDate,
        EventType eventType
) {}
