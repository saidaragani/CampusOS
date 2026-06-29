package com.campusos.academic_service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Create/replace the whole timetable for a class. */
public record TimetableRequest(
        @NotBlank String classLabel,
        @NotNull @Valid List<TimetableSlotRequest> slots
) {}
