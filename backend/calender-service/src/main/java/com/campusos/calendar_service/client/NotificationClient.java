package com.campusos.calendar_service.client;

import com.campusos.calendar_service.client.dto.AnnouncementNotification;
import com.campusos.calendar_service.client.dto.HolidayNotification;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Best-effort seam to message-service: when a holiday or announcement is
 * published, messaging fans it out to parents/teachers. Failures are swallowed
 * (logged) so publishing never breaks; degrades until message-service exists.
 */
@FeignClient(name = "message-service", path = "/api/internal/notifications")
public interface NotificationClient {

    @PostMapping("/holiday")
    void notifyHoliday(@RequestBody HolidayNotification notification);

    @PostMapping("/announcement")
    void notifyAnnouncement(@RequestBody AnnouncementNotification notification);
}
