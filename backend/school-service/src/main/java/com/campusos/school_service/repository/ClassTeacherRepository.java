package com.campusos.school_service.repository;

import com.campusos.school_service.entity.ClassTeacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClassTeacherRepository extends JpaRepository<ClassTeacher, UUID> {

    List<ClassTeacher> findBySchoolId(UUID schoolId);

    Optional<ClassTeacher> findByIdAndSchoolId(UUID id, UUID schoolId);

    Optional<ClassTeacher> findBySchoolIdAndClassLabel(UUID schoolId, String classLabel);

    boolean existsBySchoolIdAndClassLabel(UUID schoolId, String classLabel);

    /** A teacher is the class teacher of at most one class label. */
    Optional<ClassTeacher> findFirstByTeacherId(UUID teacherId);

    long countBySchoolId(UUID schoolId);
}
