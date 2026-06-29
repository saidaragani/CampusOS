package com.campusos.school_service.repository;

import com.campusos.school_service.entity.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeacherRepository extends JpaRepository<Teacher, UUID> {

    Page<Teacher> findBySchoolId(UUID schoolId, Pageable pageable);

    Optional<Teacher> findByIdAndSchoolId(UUID id, UUID schoolId);

    Optional<Teacher> findByUserId(UUID userId);

    List<Teacher> findBySchoolIdAndActiveTrue(UUID schoolId);

    boolean existsByIdAndSchoolId(UUID id, UUID schoolId);

    long countBySchoolId(UUID schoolId);
}
