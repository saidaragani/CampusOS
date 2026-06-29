package com.campusos.notification_service.scheduler;

import com.campusos.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Daily scans for birthday wishes and due festival wishes. Disable with
 * app.scheduling.enabled=false.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationScheduler {

    private final NotificationService notificationService;

    @Scheduled(cron = "${app.scheduling.birthday-cron:0 0 7 * * *}")
    public void birthdayScan() {
        int sent = notificationService.runBirthdayScan(LocalDate.now());
        log.info("Birthday scan dispatched {} wish(es).", sent);
    }

    @Scheduled(cron = "${app.scheduling.festival-cron:0 30 7 * * *}")
    public void festivalScan() {
        int sent = notificationService.runFestivalScan(LocalDate.now());
        log.info("Festival scan dispatched {} wish(es).", sent);
    }
}
