package com.campusos.academic_service.client;

import com.campusos.academic_service.client.dto.AbsenteeNotification;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Best-effort seam to message-service for absentee alerts. message-service does
 * not exist yet, so calls degrade (logged, swallowed) — attendance is never
 * blocked by email. When message-service is built (RabbitMQ per its spec), this
 * Feign call is the single swap point to publish an event instead.
 */
@FeignClient(name = "message-service", path = "/api/internal/notifications")
public interface NotificationClient {

    @PostMapping("/absentees")
    void notifyAbsentees(@RequestBody List<AbsenteeNotification> absentees);
}
