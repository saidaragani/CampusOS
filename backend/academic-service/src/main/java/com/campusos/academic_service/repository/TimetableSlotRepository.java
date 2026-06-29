package com.campusos.academic_service.repository;

import com.campusos.academic_service.entity.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, UUID> {

    List<TimetableSlot> findBySchoolIdAndClassLabelOrderByDayOfWeekAscPeriodNoAsc(UUID schoolId, String classLabel);

    List<TimetableSlot> findByTeacherIdOrderByDayOfWeekAscPeriodNoAsc(UUID teacherId);

    void deleteBySchoolIdAndClassLabel(UUID schoolId, String classLabel);
}
