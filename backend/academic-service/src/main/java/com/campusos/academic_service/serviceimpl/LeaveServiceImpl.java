package com.campusos.academic_service.serviceimpl;

import com.campusos.academic_service.dto.request.ApplyLeaveRequest;
import com.campusos.academic_service.dto.request.DecideLeaveRequest;
import com.campusos.academic_service.dto.response.LeaveResponse;
import com.campusos.academic_service.entity.LeaveRequest;
import com.campusos.academic_service.enums.LeaveStatus;
import com.campusos.academic_service.exception.BadRequestException;
import com.campusos.academic_service.exception.ForbiddenException;
import com.campusos.academic_service.exception.ResourceNotFoundException;
import com.campusos.academic_service.mapper.AcademicMappers;
import com.campusos.academic_service.repository.LeaveRequestRepository;
import com.campusos.academic_service.service.LeaveService;
import com.campusos.academic_service.support.AcademicAccessResolver;
import com.campusos.common_lib.contract.ChildLink;
import com.campusos.common_lib.contract.StudentSummary;
import com.campusos.common_lib.contract.TeacherClassView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRepository;
    private final AcademicAccessResolver access;

    @Override
    @Transactional
    public LeaveResponse applyLeave(UUID parentUserId, ApplyLeaveRequest request) {
        if (request.toDate().isBefore(request.fromDate())) {
            throw new BadRequestException("toDate cannot be before fromDate.");
        }
        ChildLink child = access.requireOwnedChild(parentUserId, request.studentId());
        StudentSummary student = access.lookupStudent(child.schoolId(), child.admissionNo());

        LeaveRequest leave = LeaveRequest.builder()
                .schoolId(child.schoolId())
                .studentId(request.studentId())
                .classLabel(student.classLabel())
                .fromDate(request.fromDate())
                .toDate(request.toDate())
                .reason(request.reason())
                .status(LeaveStatus.PENDING)
                .requestedByParentUserId(parentUserId)
                .build();
        return AcademicMappers.toLeaveResponse(leaveRepository.save(leave));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getStudentLeaves(UUID parentUserId, UUID studentId) {
        access.requireOwnedChild(parentUserId, studentId);
        return leaveRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(AcademicMappers::toLeaveResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getPendingForTeacher(UUID teacherUserId) {
        TeacherClassView tc = access.requireTeacherClass(teacherUserId);
        return leaveRepository
                .findBySchoolIdAndClassLabelAndStatusOrderByCreatedAtDesc(tc.schoolId(), tc.classLabel(), LeaveStatus.PENDING)
                .stream().map(AcademicMappers::toLeaveResponse).toList();
    }

    @Override
    @Transactional
    public LeaveResponse decideLeave(UUID teacherUserId, UUID leaveId, DecideLeaveRequest request) {
        TeacherClassView tc = access.requireTeacherClass(teacherUserId);
        LeaveRequest leave = leaveRepository.findByIdAndSchoolId(leaveId, tc.schoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found."));
        if (!leave.getClassLabel().equals(tc.classLabel())) {
            throw new ForbiddenException("That leave request is not for your class.");
        }
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("This leave request has already been decided.");
        }
        leave.setStatus(request.approve() ? LeaveStatus.APPROVED : LeaveStatus.REJECTED);
        leave.setDecidedByTeacherId(tc.teacherId());
        leave.setDecisionNote(request.note());
        leave.setDecidedAt(LocalDateTime.now());
        return AcademicMappers.toLeaveResponse(leaveRepository.save(leave));
    }
}
