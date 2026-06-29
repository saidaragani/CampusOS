package com.campusos.auth_service.client;

import com.campusos.common_lib.contract.PasswordResetNotification;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Seam to message-service. Outbound emails are best-effort: a failure here must
 * never break the originating flow (e.g. forgot-password always returns 200 so
 * accounts can't be enumerated). This is the integration point for arc step 8
 * (absentee alerts, fee reminders, holidays, birthdays, etc.).
 */
@FeignClient(name = "message-service", path = "/api/notifications")
public interface NotificationClient {

    @PostMapping("/password-reset")
    void sendPasswordReset(@RequestBody PasswordResetNotification notification);
}
