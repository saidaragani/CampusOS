package com.campusos.school_service.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignTeacherRequest(
        @NotNull(message = "Teacher id is required")
        UUID teacherId
) {}
