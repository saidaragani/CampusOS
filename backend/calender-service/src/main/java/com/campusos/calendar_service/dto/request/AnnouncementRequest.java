package com.campusos.calendar_service.dto.request;

import com.campusos.calendar_service.enums.Audience;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnnouncementRequest(
        @NotBlank String title,
        @NotBlank String body,
        @NotNull Audience audience,
        /** Required when audience = CLASS. */
        String classLabel,
        String attachmentKey
) {}
