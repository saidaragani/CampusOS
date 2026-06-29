package com.campusos.fee_service.repository;

import com.campusos.fee_service.entity.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, UUID> {

    List<FeeStructure> findBySchoolId(UUID schoolId);

    Optional<FeeStructure> findBySchoolIdAndAcademicYearAndClassLabel(UUID schoolId, String academicYear, String classLabel);

    Optional<FeeStructure> findBySchoolIdAndAcademicYearAndClassLabelIsNull(UUID schoolId, String academicYear);

    Optional<FeeStructure> findByIdAndSchoolId(UUID id, UUID schoolId);
}
