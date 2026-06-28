package com.campusos.auth_service.repository;

import com.campusos.auth_service.entity.ParentStudentLink;
import com.campusos.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParentStudentLinkRepository
        extends JpaRepository<ParentStudentLink, UUID> {

    List<ParentStudentLink> findAllByParentUser(User parentUser);

    Optional<ParentStudentLink> findBySchoolIdAndAdmissionNo(
            UUID schoolId,
            String admissionNo
    );

    boolean existsBySchoolIdAndAdmissionNo(
            UUID schoolId,
            String admissionNo
    );
}