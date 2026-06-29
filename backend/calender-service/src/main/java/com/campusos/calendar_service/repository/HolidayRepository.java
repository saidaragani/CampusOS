package com.campusos.calendar_service.repository;

import com.campusos.calendar_service.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HolidayRepository extends JpaRepository<Holiday, UUID> {

    List<Holiday> findBySchoolIdOrderByFromDateAsc(UUID schoolId);

    List<Holiday> findBySchoolIdAndFromDateBetweenOrderByFromDateAsc(UUID schoolId, LocalDate from, LocalDate to);

    Optional<Holiday> findByIdAndSchoolId(UUID id, UUID schoolId);
}
