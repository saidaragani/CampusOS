package com.campusos.school_service.controller;

import com.campusos.school_service.dto.response.UploadResponse;
import com.campusos.school_service.exception.BadRequestException;
import com.campusos.school_service.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Small dedicated upload path. Clients upload a file here, get back a URL, and
 * set it on the owning entity (e.g. PUT student photo / school logo). Files are
 * served back via the GET endpoint. Local-disk backed (swap for MinIO later).
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorage storage;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TEACHER')")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file,
                                                 @RequestParam(defaultValue = "misc") String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required.");
        }
        try {
            String key = storage.store(file.getBytes(), file.getOriginalFilename(), folder);
            return ResponseEntity.status(HttpStatus.CREATED).body(new UploadResponse(key, "/api/files/" + key));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @GetMapping("/{folder}/{filename}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> download(@PathVariable String folder, @PathVariable String filename) {
        byte[] data = storage.load(folder + "/" + filename);
        MediaType contentType = MediaTypeFactory.getMediaType(filename).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(contentType).body(data);
    }
}
