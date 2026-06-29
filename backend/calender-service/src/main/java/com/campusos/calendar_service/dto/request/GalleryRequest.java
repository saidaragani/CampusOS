package com.campusos.calendar_service.dto.request;

import com.campusos.calendar_service.enums.MediaType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Metadata for a gallery item. {@code objectKey} references the stored object
 * (MinIO later); if omitted the service generates one. Real multipart upload
 * drops in behind this contract.
 */
public record GalleryRequest(
        String title,
        @NotNull MediaType mediaType,
        UUID eventId,
        String objectKey,
        String thumbnailKey
) {}
