package com.campusos.academic_service.service;

import com.campusos.academic_service.dto.request.MarkAttendanceRequest;
import com.campusos.academic_service.dto.response.AbsenteeEntry;
import com.campusos.academic_service.dto.response.AttendanceDaySheet;
import com.campusos.academic_service.dto.response.ClassAttendanceSummary;
import com.campusos.academic_service.dto.response.StudentAttendanceView;
import com.campusos.academic_service.enums.AttendanceSession;
import com.campusos.academic_service.enums.AttendanceStatus;
import com.campusos.common_lib.contract.SchoolAttendanceStats;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AttendanceService {

    AttendanceDaySheet markAttendance(UUID teacherUserId, MarkAttendanceRequest request);

    void correctMark(UUID teacherUserId, UUID attendanceId, AttendanceStatus status);

    AttendanceDaySheet getDaySheet(UUID teacherUserId, LocalDate date, AttendanceSession session);

    List<AbsenteeEntry> getTodayAbsentees(UUID schoolId);

    List<ClassAttendanceSummary> getSummary(UUID schoolId, LocalDate from, LocalDate to);

    StudentAttendanceView getStudentAttendance(UUID parentUserId, UUID studentId, String month);

    /** Internal: school-wide attendance stats for a date range (null range → current month). */
    SchoolAttendanceStats getSchoolStats(UUID schoolId, LocalDate from, LocalDate to);
}
