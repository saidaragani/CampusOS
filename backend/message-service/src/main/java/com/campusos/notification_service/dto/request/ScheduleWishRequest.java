package com.campusos.notification_service.dto.request;

import com.campusos.notification_service.enums.WishAudience;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ScheduleWishRequest(
        @NotBlank String festivalName,
        @NotBlank String message,
        @NotNull LocalDate scheduledDate,
        WishAudience audience
) {}
