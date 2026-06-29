package com.campusos.fee_service.client;

import com.campusos.fee_service.client.dto.FeeNotification;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Best-effort seam to message-service for fee receipts + reminders. Failures are
 * swallowed (logged); degrades until message-service exists.
 */
@FeignClient(name = "message-service", path = "/api/internal/notifications")
public interface NotificationClient {

    @PostMapping("/fees")
    void notifyFees(@RequestBody List<FeeNotification> notifications);
}
