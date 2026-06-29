package com.campusos.academic_service.serviceimpl;

import com.campusos.academic_service.client.NotificationClient;
import com.campusos.academic_service.client.dto.AbsenteeNotification;
import com.campusos.academic_service.dto.request.AttendanceMark;
import com.campusos.academic_service.dto.request.MarkAttendanceRequest;
import com.campusos.academic_service.dto.response.AttendanceDaySheet;
import com.campusos.academic_service.dto.response.StudentAttendanceView;
import com.campusos.academic_service.entity.Attendance;
import com.campusos.academic_service.enums.AttendanceSession;
import com.campusos.academic_service.enums.AttendanceStatus;
import com.campusos.academic_service.exception.BadRequestException;
import com.campusos.academic_service.repository.AttendanceRepository;
import com.campusos.academic_service.support.AcademicAccessResolver;
import com.campusos.common_lib.contract.RosterStudent;
import com.campusos.common_lib.contract.TeacherClassView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private AcademicAccessResolver access;
    @Mock private NotificationClient notificationClient;

    @InjectMocks private AttendanceServiceImpl attendanceService;

    private final UUID teacherUserId = UUID.randomUUID();
    private final UUID teacherId = UUID.randomUUID();
    private final UUID schoolId = UUID.randomUUID();
    private final UUID s1 = UUID.randomUUID();
    private final UUID s2 = UUID.randomUUID();

    private TeacherClassView teacherClass() {
        return new TeacherClassView(teacherId, schoolId, "6-A");
    }

    private List<RosterStudent> roster() {
        return List.of(
                new RosterStudent(s1, schoolId, "GVS-2025-0001", "Asha", "6-A", "111", false),
                new RosterStudent(s2, schoolId, "GVS-2025-0002", "Bharat", "6-A", "222", false));
    }

    @Test
    void markAttendance_savesEachMarkAndNotifiesAbsentees() {
        when(access.requireTeacherClass(teacherUserId)).thenReturn(teacherClass());
        when(access.roster(schoolId, "6-A")).thenReturn(roster());
        when(attendanceRepository.findByStudentIdAndAttendanceDateAndSession(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));
        when(attendanceRepository.findBySchoolIdAndClassLabelAndAttendanceDateAndSession(any(), any(), any(), any()))
                .thenReturn(List.of());

        MarkAttendanceRequest request = new MarkAttendanceRequest(
                LocalDate.of(2025, 6, 20), AttendanceSession.MORNING,
                List.of(new AttendanceMark(s1, AttendanceStatus.PRESENT),
                        new AttendanceMark(s2, AttendanceStatus.ABSENT)));

        AttendanceDaySheet sheet = attendanceService.markAttendance(teacherUserId, request);

        assertThat(sheet.entries()).hasSize(2);
        verify(attendanceRepository, org.mockito.Mockito.times(2)).save(any(Attendance.class));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AbsenteeNotification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationClient).notifyAbsentees(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).studentId()).isEqualTo(s2);
    }

    @Test
    void markAttendance_studentNotInRoster_throwsBadRequest() {
        when(access.requireTeacherClass(teacherUserId)).thenReturn(teacherClass());
        when(access.roster(schoolId, "6-A")).thenReturn(roster());

        UUID stranger = UUID.randomUUID();
        MarkAttendanceRequest request = new MarkAttendanceRequest(
                LocalDate.of(2025, 6, 20), AttendanceSession.MORNING,
                List.of(new AttendanceMark(stranger, AttendanceStatus.PRESENT)));

        assertThatThrownBy(() -> attendanceService.markAttendance(teacherUserId, request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getStudentAttendance_computesMonthlySummary() {
        Attendance present1 = Attendance.builder().status(AttendanceStatus.PRESENT)
                .attendanceDate(LocalDate.of(2025, 6, 2)).session(AttendanceSession.MORNING).build();
        Attendance absent = Attendance.builder().status(AttendanceStatus.ABSENT)
                .attendanceDate(LocalDate.of(2025, 6, 3)).session(AttendanceSession.MORNING).build();
        Attendance present2 = Attendance.builder().status(AttendanceStatus.PRESENT)
                .attendanceDate(LocalDate.of(2025, 6, 4)).session(AttendanceSession.MORNING).build();
        when(attendanceRepository.findByStudentIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(eq(s1), any(), any()))
                .thenReturn(List.of(present1, absent, present2));

        StudentAttendanceView view = attendanceService.getStudentAttendance(teacherUserId, s1, "2025-06");

        assertThat(view.total()).isEqualTo(3);
        assertThat(view.present()).isEqualTo(2);
        assertThat(view.absent()).isEqualTo(1);
        assertThat(view.presentPercentage()).isEqualTo(2 * 100.0 / 3);
        assertThat(view.daily()).hasSize(3);
        verify(access).requireOwnedChild(teacherUserId, s1);
    }
}
