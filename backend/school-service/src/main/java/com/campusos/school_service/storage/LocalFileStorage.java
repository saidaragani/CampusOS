package com.campusos.school_service.storage;

import com.campusos.school_service.exception.BadRequestException;
import com.campusos.school_service.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Stores uploaded files on local disk under {@code app.storage.base-dir}. Returns
 * an object key ({@code folder/uuid.ext}). This is the swap point for MinIO/S3
 * later — controllers depend only on the key + a download URL.
 */
@Component
public class LocalFileStorage implements FileStorage {

    @Value("${app.storage.base-dir:./uploads}")
    private String baseDir;

    /** Stores the bytes and returns the object key. */
    @Override
    public String store(byte[] content, String originalFilename, String folder) {
        String safeFolder = sanitizeSegment(folder, "misc");
        String ext = extension(originalFilename);
        String name = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        Path target = base().resolve(safeFolder).resolve(name).normalize();
        if (!target.startsWith(base())) {
            throw new BadRequestException("Invalid storage path.");
        }
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return safeFolder + "/" + name;
    }

    @Override
    public byte[] load(String key) {
        Path target = base().resolve(key).normalize();
        if (!target.startsWith(base())) {
            throw new BadRequestException("Invalid file key.");
        }
        if (!Files.exists(target)) {
            throw new ResourceNotFoundException("File not found.");
        }
        try {
            return Files.readAllBytes(target);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private Path base() {
        return Path.of(baseDir).toAbsolutePath().normalize();
    }

    private String sanitizeSegment(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String cleaned = value.replaceAll("[^a-zA-Z0-9_-]", "");
        return cleaned.isBlank() ? fallback : cleaned;
    }

    private String extension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).replaceAll("[^a-zA-Z0-9]", "");
    }
}
