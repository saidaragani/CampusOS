package com.campusos.calendar_service.controller;

import com.campusos.calendar_service.dto.request.AnnouncementRequest;
import com.campusos.calendar_service.dto.response.AnnouncementResponse;
import com.campusos.calendar_service.security.AuthenticatedUser;
import com.campusos.calendar_service.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<AnnouncementResponse> post(@Valid @RequestBody AnnouncementRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(announcementService.post(user.schoolId(), user.userId(), request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','PARENT')")
    public ResponseEntity<List<AnnouncementResponse>> list(@RequestParam(required = false) String classLabel,
                                                           @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(announcementService.list(user.schoolId(), user.role(), classLabel));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<AnnouncementResponse> update(@PathVariable UUID id,
                                                       @Valid @RequestBody AnnouncementRequest request,
                                                       @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(announcementService.update(user.schoolId(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser user) {
        announcementService.delete(user.schoolId(), id);
        return ResponseEntity.noContent().build();
    }
}
