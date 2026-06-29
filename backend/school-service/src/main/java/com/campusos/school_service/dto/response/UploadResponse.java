package com.campusos.school_service.dto.response;

/**
 * Result of a file upload. {@code key} is the stored object key; {@code url} is
 * the download path to persist on the owning entity (e.g. student.photoUrl).
 */
public record UploadResponse(
        String key,
        String url
) {}
