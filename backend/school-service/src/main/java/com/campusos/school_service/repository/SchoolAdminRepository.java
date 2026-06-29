package com.campusos.school_service.repository;

import com.campusos.school_service.entity.SchoolAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SchoolAdminRepository extends JpaRepository<SchoolAdmin, UUID> {

    boolean existsBySchoolId(UUID schoolId);

    Optional<SchoolAdmin> findBySchoolId(UUID schoolId);
}
