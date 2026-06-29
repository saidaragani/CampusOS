package com.campusos.school_service.repository;

import com.campusos.school_service.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {

    Page<Student> findBySchoolId(UUID schoolId, Pageable pageable);

    Page<Student> findBySchoolIdAndClassLabel(UUID schoolId, String classLabel, Pageable pageable);

    List<Student> findBySchoolIdAndClassLabelAndActiveTrue(UUID schoolId, String classLabel);

    Optional<Student> findByIdAndSchoolId(UUID id, UUID schoolId);

    Optional<Student> findBySchoolIdAndAdmissionNo(UUID schoolId, String admissionNo);

    boolean existsBySchoolIdAndAdmissionNo(UUID schoolId, String admissionNo);

    long countBySchoolId(UUID schoolId);

    /** Birthday scan across all schools (for the messaging service). */
    @Query("select s from Student s where function('MONTH', s.dateOfBirth) = :month "
            + "and function('DAY', s.dateOfBirth) = :day and s.active = true")
    List<Student> findByBirthday(@Param("month") int month, @Param("day") int day);
}
