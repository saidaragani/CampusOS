package com.campusos.notification_service.repository;

import com.campusos.notification_service.entity.WishSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WishScheduleRepository extends JpaRepository<WishSchedule, UUID> {

    List<WishSchedule> findBySchoolIdOrderByScheduledDateAsc(UUID schoolId);

    Optional<WishSchedule> findByIdAndSchoolId(UUID id, UUID schoolId);

    List<WishSchedule> findBySentFalseAndScheduledDate(LocalDate scheduledDate);
}
