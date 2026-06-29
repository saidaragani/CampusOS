package com.campusos.notification_service.serviceimpl;

import com.campusos.common_lib.contract.RecipientContact;
import com.campusos.common_lib.contract.RosterStudent;
import com.campusos.notification_service.client.AuthClient;
import com.campusos.notification_service.client.SchoolClient;
import com.campusos.notification_service.client.dto.BirthdayStudent;
import com.campusos.notification_service.dto.event.AbsenteeEvent;
import com.campusos.notification_service.dto.event.AnnouncementEvent;
import com.campusos.notification_service.dto.event.FeeEvent;
import com.campusos.notification_service.dto.event.HolidayEvent;
import com.campusos.notification_service.dto.request.ScheduleWishRequest;
import com.campusos.notification_service.dto.response.NotificationLogResponse;
import com.campusos.notification_service.dto.response.WishResponse;
import com.campusos.notification_service.email.EmailSender;
import com.campusos.notification_service.entity.NotificationLog;
import com.campusos.notification_service.entity.WishSchedule;
import com.campusos.notification_service.enums.Channel;
import com.campusos.notification_service.enums.NotificationStatus;
import com.campusos.notification_service.enums.NotificationTemplate;
import com.campusos.notification_service.enums.WishAudience;
import com.campusos.notification_service.exception.BadRequestException;
import com.campusos.notification_service.exception.ResourceNotFoundException;
import com.campusos.notification_service.mapper.NotificationMappers;
import com.campusos.notification_service.repository.NotificationLogRepository;
import com.campusos.notification_service.repository.WishScheduleRepository;
import com.campusos.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogRepository logRepository;
    private final WishScheduleRepository wishRepository;
    private final AuthClient authClient;
    private final SchoolClient schoolClient;
    private final EmailSender emailSender;

    // ------------------------------------------------------------------
    // Event handlers
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public void handleAbsentees(List<AbsenteeEvent> events) {
        for (AbsenteeEvent e : events) {
            String subject = "Attendance Alert";
            String body = "Your child was marked absent on " + e.date() + " (" + e.session() + ").";
            dispatch(e.schoolId(), studentParents(e.studentId()),
                    NotificationTemplate.ABSENT, subject, body, "attendance.absent");
        }
    }

    @Override
    @Transactional
    public void handleFees(List<FeeEvent> events) {
        for (FeeEvent e : events) {
            boolean receipt = "RECEIPT".equalsIgnoreCase(e.kind());
            NotificationTemplate template = receipt ? NotificationTemplate.FEE_RECEIPT : NotificationTemplate.FEE_REMINDER;
            String subject = receipt ? "Fee Payment Receipt" : "Fee Payment Reminder";
            String body = (receipt ? "Payment received for " : "Payment pending for ")
                    + e.feeType() + " fee: amount " + e.amount() + ".";
            dispatch(e.schoolId(), studentParents(e.studentId()), template, subject, body, "fee.status");
        }
    }

    @Override
    @Transactional
    public void handleHoliday(HolidayEvent event) {
        String subject = "Holiday: " + event.name();
        String body = event.name() + " — from " + event.fromDate() + " to " + event.toDate() + ".";
        dispatch(event.schoolId(), schoolParents(event.schoolId()),
                NotificationTemplate.HOLIDAY, subject, body, "holiday.published");
    }

    @Override
    @Transactional
    public void handleAnnouncement(AnnouncementEvent event) {
        List<RecipientContact> recipients = switch (event.audience() == null ? "" : event.audience()) {
            case "TEACHERS" -> teacherRecipients(event.schoolId());
            case "CLASS" -> classParents(event.schoolId(), event.classLabel());
            default -> schoolParents(event.schoolId());
        };
        dispatch(event.schoolId(), recipients, NotificationTemplate.ANNOUNCEMENT,
                event.title(), event.body(), "announcement.published");
    }

    // ------------------------------------------------------------------
    // Scheduled scans
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public int runBirthdayScan(LocalDate date) {
        int sent = 0;
        for (BirthdayStudent student : birthdays(date)) {
            String subject = "Happy Birthday, " + student.fullName() + "!";
            String body = "Wishing " + student.fullName() + " a very happy birthday from all of us!";
            sent += dispatch(student.schoolId(), studentParents(student.studentId()),
                    NotificationTemplate.BIRTHDAY, subject, body, "birthday");
        }
        return sent;
    }

    @Override
    @Transactional
    public int runFestivalScan(LocalDate date) {
        int sent = 0;
        for (WishSchedule wish : wishRepository.findBySentFalseAndScheduledDate(date)) {
            List<RecipientContact> recipients = wish.getAudience() == WishAudience.TEACHERS
                    ? teacherRecipients(wish.getSchoolId())
                    : schoolParents(wish.getSchoolId());
            sent += dispatch(wish.getSchoolId(), recipients, NotificationTemplate.FESTIVAL,
                    wish.getFestivalName(), wish.getMessage(), "festival");
            wish.setSent(true);
            wishRepository.save(wish);
        }
        return sent;
    }

    // ------------------------------------------------------------------
    // Admin surface
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public WishResponse scheduleWish(UUID schoolId, UUID userId, ScheduleWishRequest request) {
        WishSchedule wish = WishSchedule.builder()
                .schoolId(schoolId)
                .festivalName(request.festivalName())
                .message(request.message())
                .scheduledDate(request.scheduledDate())
                .audience(request.audience() != null ? request.audience() : WishAudience.ALL_PARENTS)
                .sent(false)
                .createdByUserId(userId)
                .build();
        return NotificationMappers.toWishResponse(wishRepository.save(wish));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishResponse> listWishes(UUID schoolId) {
        return wishRepository.findBySchoolIdOrderByScheduledDateAsc(schoolId).stream()
                .map(NotificationMappers::toWishResponse).toList();
    }

    @Override
    @Transactional
    public void cancelWish(UUID schoolId, UUID id) {
        WishSchedule wish = wishRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled wish not found."));
        if (wish.isSent()) {
            throw new BadRequestException("This wish has already been sent.");
        }
        wishRepository.delete(wish);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationLogResponse> getLog(UUID schoolId, NotificationStatus status, Pageable pageable) {
        Page<NotificationLog> page = (status == null)
                ? logRepository.findBySchoolId(schoolId, pageable)
                : logRepository.findBySchoolIdAndStatus(schoolId, status, pageable);
        return page.map(NotificationMappers::toLogResponse);
    }

    @Override
    @Transactional
    public NotificationLogResponse retry(UUID schoolId, UUID id) {
        NotificationLog logEntry = logRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found."));
        if (logEntry.getStatus() == NotificationStatus.SENT) {
            throw new BadRequestException("This notification was already sent.");
        }
        try {
            emailSender.send(logEntry.getRecipientEmail(), logEntry.getSubject(), logEntry.getSubject());
            logEntry.setStatus(NotificationStatus.SENT);
            logEntry.setErrorMessage(null);
            logEntry.setSentAt(LocalDateTime.now());
        } catch (Exception ex) {
            logEntry.setStatus(NotificationStatus.FAILED);
            logEntry.setErrorMessage(truncate(ex.getMessage()));
        }
        return NotificationMappers.toLogResponse(logRepository.save(logEntry));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Sends to each recipient and records a log row. Returns how many were sent OK. */
    private int dispatch(UUID schoolId, List<RecipientContact> recipients, NotificationTemplate template,
                         String subject, String body, String routingKey) {
        int ok = 0;
        for (RecipientContact recipient : recipients) {
            if (recipient.email() == null || recipient.email().isBlank()) {
                continue;
            }
            NotificationLog logEntry = NotificationLog.builder()
                    .schoolId(schoolId)
                    .channel(Channel.EMAIL)
                    .recipientEmail(recipient.email())
                    .subject(subject)
                    .template(template)
                    .relatedRoutingKey(routingKey)
                    .build();
            try {
                emailSender.send(recipient.email(), subject, body);
                logEntry.setStatus(NotificationStatus.SENT);
                logEntry.setSentAt(LocalDateTime.now());
                ok++;
            } catch (Exception ex) {
                logEntry.setStatus(NotificationStatus.FAILED);
                logEntry.setErrorMessage(truncate(ex.getMessage()));
            }
            logRepository.save(logEntry);
        }
        return ok;
    }

    private List<RecipientContact> studentParents(UUID studentId) {
        try {
            return authClient.studentParentRecipients(studentId);
        } catch (Exception ex) {
            log.warn("Failed to resolve parents for student {}: {}", studentId, ex.getMessage());
            return Collections.emptyList();
        }
    }

    private List<RecipientContact> schoolParents(UUID schoolId) {
        try {
            return authClient.schoolParentRecipients(schoolId);
        } catch (Exception ex) {
            log.warn("Failed to resolve school parents for {}: {}", schoolId, ex.getMessage());
            return Collections.emptyList();
        }
    }

    private List<RecipientContact> teacherRecipients(UUID schoolId) {
        try {
            return schoolClient.teacherRecipients(schoolId);
        } catch (Exception ex) {
            log.warn("Failed to resolve teachers for {}: {}", schoolId, ex.getMessage());
            return Collections.emptyList();
        }
    }

    private List<RecipientContact> classParents(UUID schoolId, String classLabel) {
        if (classLabel == null) {
            return Collections.emptyList();
        }
        try {
            List<RosterStudent> roster = schoolClient.getRoster(schoolId, classLabel);
            return roster.stream()
                    .flatMap(s -> studentParents(s.studentId()).stream())
                    .filter(distinctByEmail())
                    .toList();
        } catch (Exception ex) {
            log.warn("Failed to resolve class parents for {}/{}: {}", schoolId, classLabel, ex.getMessage());
            return Collections.emptyList();
        }
    }

    private List<BirthdayStudent> birthdays(LocalDate date) {
        try {
            return schoolClient.birthdays(date);
        } catch (Exception ex) {
            log.warn("Failed to load birthdays for {}: {}", date, ex.getMessage());
            return Collections.emptyList();
        }
    }

    private java.util.function.Predicate<RecipientContact> distinctByEmail() {
        java.util.Set<String> seen = java.util.concurrent.ConcurrentHashMap.newKeySet();
        return r -> r.email() != null && seen.add(r.email());
    }

    private String truncate(String message) {
        if (message == null) {
            return "Unknown error";
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
