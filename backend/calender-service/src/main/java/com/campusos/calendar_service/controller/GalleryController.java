package com.campusos.calendar_service.controller;

import com.campusos.calendar_service.dto.request.GalleryRequest;
import com.campusos.calendar_service.dto.response.GalleryResponse;
import com.campusos.calendar_service.dto.response.GalleryUrlResponse;
import com.campusos.calendar_service.security.AuthenticatedUser;
import com.campusos.calendar_service.service.GalleryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/gallery")
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryService galleryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<GalleryResponse> upload(@Valid @RequestBody GalleryRequest request,
                                                  @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(galleryService.upload(user.schoolId(), user.userId(), request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','PARENT')")
    public ResponseEntity<Page<GalleryResponse>> browse(@RequestParam(required = false) UUID eventId,
                                                        @PageableDefault(size = 20) Pageable pageable,
                                                        @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(galleryService.browse(user.schoolId(), eventId, pageable));
    }

    @GetMapping("/{id}/url")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','PARENT')")
    public ResponseEntity<GalleryUrlResponse> url(@PathVariable UUID id,
                                                  @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(galleryService.url(user.schoolId(), id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser user) {
        galleryService.delete(user.schoolId(), id);
        return ResponseEntity.noContent().build();
    }
}
