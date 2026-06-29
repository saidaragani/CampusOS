package com.campusos.calendar_service.repository;

import com.campusos.calendar_service.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {

    List<Announcement> findBySchoolIdOrderByCreatedAtDesc(UUID schoolId);

    Optional<Announcement> findByIdAndSchoolId(UUID id, UUID schoolId);
}
