package com.campusos.notification_service.repository;

import com.campusos.notification_service.entity.NotificationLog;
import com.campusos.notification_service.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    Page<NotificationLog> findBySchoolId(UUID schoolId, Pageable pageable);

    Page<NotificationLog> findBySchoolIdAndStatus(UUID schoolId, NotificationStatus status, Pageable pageable);

    Optional<NotificationLog> findByIdAndSchoolId(UUID id, UUID schoolId);
}
