package com.campusos.calendar_service.serviceimpl;

import com.campusos.calendar_service.dto.request.GalleryRequest;
import com.campusos.calendar_service.dto.response.GalleryResponse;
import com.campusos.calendar_service.dto.response.GalleryUrlResponse;
import com.campusos.calendar_service.entity.GalleryItem;
import com.campusos.calendar_service.exception.ResourceNotFoundException;
import com.campusos.calendar_service.mapper.CalendarMappers;
import com.campusos.calendar_service.repository.GalleryItemRepository;
import com.campusos.calendar_service.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GalleryServiceImpl implements GalleryService {

    private final GalleryItemRepository galleryItemRepository;

    @Override
    @Transactional
    public GalleryResponse upload(UUID schoolId, UUID userId, GalleryRequest request) {
        String objectKey = (request.objectKey() != null && !request.objectKey().isBlank())
                ? request.objectKey()
                : "gallery/" + schoolId + "/" + UUID.randomUUID();

        GalleryItem item = GalleryItem.builder()
                .schoolId(schoolId)
                .eventId(request.eventId())
                .title(request.title())
                .mediaType(request.mediaType())
                .objectKey(objectKey)
                .thumbnailKey(request.thumbnailKey())
                .uploadedByUserId(userId)
                .build();
        return CalendarMappers.toGalleryResponse(galleryItemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GalleryResponse> browse(UUID schoolId, UUID eventId, Pageable pageable) {
        Page<GalleryItem> page = (eventId == null)
                ? galleryItemRepository.findBySchoolId(schoolId, pageable)
                : galleryItemRepository.findBySchoolIdAndEventId(schoolId, eventId, pageable);
        return page.map(CalendarMappers::toGalleryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public GalleryUrlResponse url(UUID schoolId, UUID id) {
        GalleryItem item = require(schoolId, id);
        // Stub presigned URL — real MinIO presign drops in behind this contract.
        return new GalleryUrlResponse("/files/" + item.getObjectKey());
    }

    @Override
    @Transactional
    public void delete(UUID schoolId, UUID id) {
        galleryItemRepository.delete(require(schoolId, id));
    }

    private GalleryItem require(UUID schoolId, UUID id) {
        return galleryItemRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Gallery item not found."));
    }
}
