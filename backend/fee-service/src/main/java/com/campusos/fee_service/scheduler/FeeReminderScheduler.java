package com.campusos.fee_service.scheduler;

import com.campusos.fee_service.client.NotificationClient;
import com.campusos.fee_service.client.dto.FeeNotification;
import com.campusos.fee_service.entity.StudentFee;
import com.campusos.fee_service.enums.FeeStatus;
import com.campusos.fee_service.repository.StudentFeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Daily scan of overdue pending fees → best-effort REMINDER notifications to
 * message-service. Disable with app.fee.reminders.enabled=false.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.fee.reminders.enabled", havingValue = "true", matchIfMissing = true)
public class FeeReminderScheduler {

    private final StudentFeeRepository studentFeeRepository;
    private final NotificationClient notificationClient;

    @Scheduled(cron = "${app.fee.reminders.cron:0 0 8 * * *}")
    public void sendReminders() {
        List<StudentFee> overdue = studentFeeRepository
                .findByStatusAndDueDateLessThanEqual(FeeStatus.PENDING, LocalDate.now());
        if (overdue.isEmpty()) {
            return;
        }
        List<FeeNotification> notifications = overdue.stream()
                .map(f -> new FeeNotification(f.getStudentId(), f.getSchoolId(),
                        f.getFeeType().name(), "REMINDER", f.getAmount()))
                .toList();
        try {
            notificationClient.notifyFees(notifications);
            log.info("Dispatched {} fee reminder(s).", notifications.size());
        } catch (Exception ex) {
            log.warn("Failed to dispatch fee reminders: {}", ex.getMessage());
        }
    }
}
