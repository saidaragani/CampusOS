package com.campusos.notification_service.service;

import com.campusos.notification_service.dto.event.AbsenteeEvent;
import com.campusos.notification_service.dto.event.AnnouncementEvent;
import com.campusos.notification_service.dto.event.FeeEvent;
import com.campusos.notification_service.dto.event.HolidayEvent;
import com.campusos.notification_service.dto.request.ScheduleWishRequest;
import com.campusos.notification_service.dto.response.NotificationLogResponse;
import com.campusos.notification_service.dto.response.WishResponse;
import com.campusos.notification_service.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

    // --- event-driven (called by other services) ---
    void handleAbsentees(List<AbsenteeEvent> events);

    void handleFees(List<FeeEvent> events);

    void handleHoliday(HolidayEvent event);

    void handleAnnouncement(AnnouncementEvent event);

    // --- scheduled scans (called by schedulers) ---
    int runBirthdayScan(LocalDate date);

    int runFestivalScan(LocalDate date);

    // --- admin surface ---
    WishResponse scheduleWish(UUID schoolId, UUID userId, ScheduleWishRequest request);

    List<WishResponse> listWishes(UUID schoolId);

    void cancelWish(UUID schoolId, UUID id);

    Page<NotificationLogResponse> getLog(UUID schoolId, NotificationStatus status, Pageable pageable);

    NotificationLogResponse retry(UUID schoolId, UUID id);
}
