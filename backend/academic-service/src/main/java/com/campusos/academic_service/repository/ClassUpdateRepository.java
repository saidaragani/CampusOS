package com.campusos.academic_service.repository;

import com.campusos.academic_service.entity.ClassUpdate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassUpdateRepository extends JpaRepository<ClassUpdate, UUID> {

    List<ClassUpdate> findBySchoolIdAndClassLabelOrderByCreatedAtDesc(UUID schoolId, String classLabel);

    Optional<ClassUpdate> findByIdAndSchoolId(UUID id, UUID schoolId);
}
