package com.campusos.notification_service.controller;

import com.campusos.notification_service.dto.event.AbsenteeEvent;
import com.campusos.notification_service.dto.event.AnnouncementEvent;
import com.campusos.notification_service.dto.event.FeeEvent;
import com.campusos.notification_service.dto.event.HolidayEvent;
import com.campusos.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Service-to-service entry points (NOT routed by the gateway). Other services
 * post events here best-effort; this service resolves recipients and emails them.
 */
@RestController
@RequestMapping("/api/internal/notifications")
@RequiredArgsConstructor
public class NotificationInternalController {

    private final NotificationService notificationService;

    @PostMapping("/absentees")
    public ResponseEntity<Void> absentees(@RequestBody List<AbsenteeEvent> events) {
        notificationService.handleAbsentees(events);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/fees")
    public ResponseEntity<Void> fees(@RequestBody List<FeeEvent> events) {
        notificationService.handleFees(events);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/holiday")
    public ResponseEntity<Void> holiday(@RequestBody HolidayEvent event) {
        notificationService.handleHoliday(event);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/announcement")
    public ResponseEntity<Void> announcement(@RequestBody AnnouncementEvent event) {
        notificationService.handleAnnouncement(event);
        return ResponseEntity.accepted().build();
    }
}
