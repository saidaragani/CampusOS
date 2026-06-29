package com.campusos.academic_service.serviceimpl;

import com.campusos.academic_service.dto.request.ApplyLeaveRequest;
import com.campusos.academic_service.dto.request.DecideLeaveRequest;
import com.campusos.academic_service.dto.response.LeaveResponse;
import com.campusos.academic_service.entity.LeaveRequest;
import com.campusos.academic_service.enums.LeaveStatus;
import com.campusos.academic_service.exception.BadRequestException;
import com.campusos.academic_service.repository.LeaveRequestRepository;
import com.campusos.academic_service.support.AcademicAccessResolver;
import com.campusos.common_lib.contract.ChildLink;
import com.campusos.common_lib.contract.StudentSummary;
import com.campusos.common_lib.contract.TeacherClassView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaveServiceImplTest {

    @Mock private LeaveRequestRepository leaveRepository;
    @Mock private AcademicAccessResolver access;

    @InjectMocks private LeaveServiceImpl leaveService;

    private final UUID parentUserId = UUID.randomUUID();
    private final UUID teacherUserId = UUID.randomUUID();
    private final UUID teacherId = UUID.randomUUID();
    private final UUID schoolId = UUID.randomUUID();
    private final UUID studentId = UUID.randomUUID();

    @Test
    void applyLeave_createsPendingLeaveWithResolvedClass() {
        when(access.requireOwnedChild(parentUserId, studentId))
                .thenReturn(new ChildLink(studentId, schoolId, "GVS-2025-0001"));
        when(access.lookupStudent(schoolId, "GVS-2025-0001"))
                .thenReturn(new StudentSummary(studentId, schoolId, "GVS-2025-0001", "Asha", "6-A", "A", "Ramesh"));
        when(leaveRepository.save(any(LeaveRequest.class))).thenAnswer(i -> i.getArgument(0));

        LeaveResponse response = leaveService.applyLeave(parentUserId, new ApplyLeaveRequest(
                studentId, LocalDate.of(2025, 6, 10), LocalDate.of(2025, 6, 11), "Fever"));

        assertThat(response.status()).isEqualTo(LeaveStatus.PENDING);
        assertThat(response.classLabel()).isEqualTo("6-A");
    }

    @Test
    void applyLeave_toDateBeforeFromDate_throws() {
        assertThatThrownBy(() -> leaveService.applyLeave(parentUserId, new ApplyLeaveRequest(
                studentId, LocalDate.of(2025, 6, 11), LocalDate.of(2025, 6, 10), "Fever")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void decideLeave_approvesPendingLeave() {
        when(access.requireTeacherClass(teacherUserId)).thenReturn(new TeacherClassView(teacherId, schoolId, "6-A"));
        UUID leaveId = UUID.randomUUID();
        LeaveRequest leave = LeaveRequest.builder()
                .id(leaveId).schoolId(schoolId).studentId(studentId).classLabel("6-A")
                .fromDate(LocalDate.of(2025, 6, 10)).toDate(LocalDate.of(2025, 6, 11)).reason("Fever")
                .status(LeaveStatus.PENDING).requestedByParentUserId(parentUserId).build();
        when(leaveRepository.findByIdAndSchoolId(leaveId, schoolId)).thenReturn(Optional.of(leave));
        when(leaveRepository.save(any(LeaveRequest.class))).thenAnswer(i -> i.getArgument(0));

        LeaveResponse response = leaveService.decideLeave(teacherUserId, leaveId, new DecideLeaveRequest(true, "OK"));

        assertThat(response.status()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(leave.getDecidedByTeacherId()).isEqualTo(teacherId);
        assertThat(leave.getDecidedAt()).isNotNull();
    }

    @Test
    void decideLeave_alreadyDecided_throws() {
        when(access.requireTeacherClass(teacherUserId)).thenReturn(new TeacherClassView(teacherId, schoolId, "6-A"));
        UUID leaveId = UUID.randomUUID();
        LeaveRequest leave = LeaveRequest.builder()
                .id(leaveId).schoolId(schoolId).studentId(studentId).classLabel("6-A")
                .fromDate(LocalDate.of(2025, 6, 10)).toDate(LocalDate.of(2025, 6, 11)).reason("Fever")
                .status(LeaveStatus.APPROVED).requestedByParentUserId(parentUserId).build();
        when(leaveRepository.findByIdAndSchoolId(leaveId, schoolId)).thenReturn(Optional.of(leave));

        assertThatThrownBy(() -> leaveService.decideLeave(teacherUserId, leaveId, new DecideLeaveRequest(true, "OK")))
                .isInstanceOf(BadRequestException.class);
    }
}
