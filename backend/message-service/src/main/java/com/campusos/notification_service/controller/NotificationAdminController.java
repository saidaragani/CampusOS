package com.campusos.notification_service.controller;

import com.campusos.notification_service.dto.request.ScheduleWishRequest;
import com.campusos.notification_service.dto.response.NotificationLogResponse;
import com.campusos.notification_service.dto.response.WishResponse;
import com.campusos.notification_service.enums.NotificationStatus;
import com.campusos.notification_service.security.AuthenticatedUser;
import com.campusos.notification_service.service.NotificationService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationAdminController {

    private final NotificationService notificationService;

    @PostMapping("/festival")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WishResponse> scheduleFestival(@Valid @RequestBody ScheduleWishRequest request,
                                                         @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(notificationService.scheduleWish(user.schoolId(), user.userId(), request));
    }

    @GetMapping("/festival")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<WishResponse>> listFestivals(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(notificationService.listWishes(user.schoolId()));
    }

    @DeleteMapping("/festival/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelFestival(@PathVariable UUID id,
                                               @AuthenticationPrincipal AuthenticatedUser user) {
        notificationService.cancelWish(user.schoolId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/log")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationLogResponse>> log(@RequestParam(required = false) NotificationStatus status,
                                                            @PageableDefault(size = 20) Pageable pageable,
                                                            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(notificationService.getLog(user.schoolId(), status, pageable));
    }

    @PostMapping("/{id}/retry")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationLogResponse> retry(@PathVariable UUID id,
                                                         @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(notificationService.retry(user.schoolId(), id));
    }
}
