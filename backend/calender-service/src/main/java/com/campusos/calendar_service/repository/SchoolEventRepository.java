package com.campusos.calendar_service.repository;

import com.campusos.calendar_service.entity.SchoolEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SchoolEventRepository extends JpaRepository<SchoolEvent, UUID> {

    List<SchoolEvent> findBySchoolIdOrderByEventDateAsc(UUID schoolId);

    List<SchoolEvent> findBySchoolIdAndEventDateBetweenOrderByEventDateAsc(UUID schoolId, LocalDate from, LocalDate to);

    List<SchoolEvent> findBySchoolIdAndEventDateGreaterThanEqualOrderByEventDateAsc(UUID schoolId, LocalDate from);

    Optional<SchoolEvent> findByIdAndSchoolId(UUID id, UUID schoolId);
}
