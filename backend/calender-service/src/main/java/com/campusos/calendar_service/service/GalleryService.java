package com.campusos.calendar_service.service;

import com.campusos.calendar_service.dto.request.GalleryRequest;
import com.campusos.calendar_service.dto.response.GalleryResponse;
import com.campusos.calendar_service.dto.response.GalleryUrlResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GalleryService {

    GalleryResponse upload(UUID schoolId, UUID userId, GalleryRequest request);

    Page<GalleryResponse> browse(UUID schoolId, UUID eventId, Pageable pageable);

    GalleryUrlResponse url(UUID schoolId, UUID id);

    void delete(UUID schoolId, UUID id);
}
