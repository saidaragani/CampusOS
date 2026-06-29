package com.campusos.calendar_service.dto.response;

import com.campusos.calendar_service.enums.EventType;

import java.time.LocalDate;
import java.util.UUID;

public record EventResponse(
        UUID id,
        UUID schoolId,
        String title,
        String description,
        LocalDate eventDate,
        EventType eventType
) {}
