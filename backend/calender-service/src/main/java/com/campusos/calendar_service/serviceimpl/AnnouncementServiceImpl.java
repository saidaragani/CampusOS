package com.campusos.calendar_service.serviceimpl;

import com.campusos.calendar_service.client.NotificationClient;
import com.campusos.calendar_service.client.dto.AnnouncementNotification;
import com.campusos.calendar_service.dto.request.AnnouncementRequest;
import com.campusos.calendar_service.dto.response.AnnouncementResponse;
import com.campusos.calendar_service.entity.Announcement;
import com.campusos.calendar_service.enums.Audience;
import com.campusos.calendar_service.exception.BadRequestException;
import com.campusos.calendar_service.exception.ResourceNotFoundException;
import com.campusos.calendar_service.mapper.CalendarMappers;
import com.campusos.calendar_service.repository.AnnouncementRepository;
import com.campusos.calendar_service.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public AnnouncementResponse post(UUID schoolId, UUID userId, AnnouncementRequest request) {
        if (request.audience() == Audience.CLASS && (request.classLabel() == null || request.classLabel().isBlank())) {
            throw new BadRequestException("classLabel is required when audience is CLASS.");
        }
        Announcement announcement = Announcement.builder()
                .schoolId(schoolId)
                .title(request.title())
                .body(request.body())
                .audience(request.audience())
                .classLabel(request.audience() == Audience.CLASS ? request.classLabel() : null)
                .attachmentKey(request.attachmentKey())
                .postedByUserId(userId)
                .build();
        Announcement saved = announcementRepository.save(announcement);

        try {
            notificationClient.notifyAnnouncement(new AnnouncementNotification(
                    schoolId, saved.getAudience().name(), saved.getClassLabel(), saved.getTitle(), saved.getBody()));
        } catch (Exception ex) {
            log.warn("Failed to publish announcement notification: {}", ex.getMessage());
        }
        return CalendarMappers.toAnnouncementResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> list(UUID schoolId, String role, String classLabel) {
        return announcementRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId).stream()
                .filter(a -> visibleTo(a, role, classLabel))
                .map(CalendarMappers::toAnnouncementResponse)
                .toList();
    }

    @Override
    @Transactional
    public AnnouncementResponse update(UUID schoolId, UUID id, AnnouncementRequest request) {
        Announcement announcement = require(schoolId, id);
        announcement.setTitle(request.title());
        announcement.setBody(request.body());
        announcement.setAudience(request.audience());
        announcement.setClassLabel(request.audience() == Audience.CLASS ? request.classLabel() : null);
        announcement.setAttachmentKey(request.attachmentKey());
        return CalendarMappers.toAnnouncementResponse(announcementRepository.save(announcement));
    }

    @Override
    @Transactional
    public void delete(UUID schoolId, UUID id) {
        announcementRepository.delete(require(schoolId, id));
    }

    private boolean visibleTo(Announcement a, String role, String classLabel) {
        if ("ADMIN".equals(role)) {
            return true;
        }
        boolean ownClass = a.getAudience() == Audience.CLASS
                && classLabel != null && classLabel.equals(a.getClassLabel());
        if ("TEACHER".equals(role)) {
            return a.getAudience() == Audience.TEACHERS || a.getAudience() == Audience.ALL_PARENTS || ownClass;
        }
        // PARENT
        return a.getAudience() == Audience.ALL_PARENTS || ownClass;
    }

    private Announcement require(UUID schoolId, UUID id) {
        return announcementRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found."));
    }
}
