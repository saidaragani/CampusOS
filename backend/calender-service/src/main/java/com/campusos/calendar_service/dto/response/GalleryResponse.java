package com.campusos.calendar_service.dto.response;

import com.campusos.calendar_service.enums.MediaType;

import java.time.LocalDateTime;
import java.util.UUID;

public record GalleryResponse(
        UUID id,
        UUID schoolId,
        UUID eventId,
        String title,
        MediaType mediaType,
        String objectKey,
        String thumbnailKey,
        LocalDateTime createdAt
) {}
