package com.campusos.calendar_service.repository;

import com.campusos.calendar_service.entity.GalleryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GalleryItemRepository extends JpaRepository<GalleryItem, UUID> {

    Page<GalleryItem> findBySchoolId(UUID schoolId, Pageable pageable);

    Page<GalleryItem> findBySchoolIdAndEventId(UUID schoolId, UUID eventId, Pageable pageable);

    Optional<GalleryItem> findByIdAndSchoolId(UUID id, UUID schoolId);
}
