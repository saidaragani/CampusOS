package com.campusos.academic_service.repository;

import com.campusos.academic_service.entity.LeaveRequest;
import com.campusos.academic_service.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    List<LeaveRequest> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    List<LeaveRequest> findBySchoolIdAndClassLabelAndStatusOrderByCreatedAtDesc(
            UUID schoolId, String classLabel, LeaveStatus status);

    Optional<LeaveRequest> findByIdAndSchoolId(UUID id, UUID schoolId);
}
