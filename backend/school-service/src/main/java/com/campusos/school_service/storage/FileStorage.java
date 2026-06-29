package com.campusos.school_service.storage;

/**
 * Storage seam. {@link LocalFileStorage} is the default (local disk). A
 * {@code MinioFileStorage} implementing this interface drops in for production
 * with no controller changes — mark the chosen impl {@code @Primary}.
 */
public interface FileStorage {

    /** Stores the bytes and returns the object key ({@code folder/uuid.ext}). */
    String store(byte[] content, String originalFilename, String folder);

    byte[] load(String key);
}
