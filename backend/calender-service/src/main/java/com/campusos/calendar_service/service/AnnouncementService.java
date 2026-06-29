package com.campusos.calendar_service.service;

import com.campusos.calendar_service.dto.request.AnnouncementRequest;
import com.campusos.calendar_service.dto.response.AnnouncementResponse;

import java.util.List;
import java.util.UUID;

public interface AnnouncementService {

    AnnouncementResponse post(UUID schoolId, UUID userId, AnnouncementRequest request);

    /** Filtered by the viewer's role and (for class-scoped items) their class label. */
    List<AnnouncementResponse> list(UUID schoolId, String role, String classLabel);

    AnnouncementResponse update(UUID schoolId, UUID id, AnnouncementRequest request);

    void delete(UUID schoolId, UUID id);
}
