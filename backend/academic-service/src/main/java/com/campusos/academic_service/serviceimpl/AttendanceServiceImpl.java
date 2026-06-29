package com.campusos.academic_service.serviceimpl;

import com.campusos.academic_service.client.NotificationClient;
import com.campusos.academic_service.client.dto.AbsenteeNotification;
import com.campusos.academic_service.dto.request.AttendanceMark;
import com.campusos.academic_service.dto.request.MarkAttendanceRequest;
import com.campusos.academic_service.dto.response.*;
import com.campusos.academic_service.entity.Attendance;
import com.campusos.academic_service.enums.AttendanceSession;
import com.campusos.academic_service.enums.AttendanceStatus;
import com.campusos.academic_service.exception.BadRequestException;
import com.campusos.academic_service.exception.ForbiddenException;
import com.campusos.academic_service.exception.ResourceNotFoundException;
import com.campusos.academic_service.mapper.AcademicMappers;
import com.campusos.academic_service.repository.AttendanceRepository;
import com.campusos.academic_service.service.AttendanceService;
import com.campusos.academic_service.support.AcademicAccessResolver;
import com.campusos.common_lib.contract.RosterStudent;
import com.campusos.common_lib.contract.SchoolAttendanceStats;
import com.campusos.common_lib.contract.TeacherClassView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AcademicAccessResolver access;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public AttendanceDaySheet markAttendance(UUID teacherUserId, MarkAttendanceRequest request) {
        TeacherClassView tc = access.requireTeacherClass(teacherUserId);
        List<RosterStudent> roster = access.roster(tc.schoolId(), tc.classLabel());
        Set<UUID> rosterIds = roster.stream().map(RosterStudent::studentId).collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();
        List<AbsenteeNotification> absentees = new ArrayList<>();

        for (AttendanceMark mark : request.marks()) {
            if (!rosterIds.contains(mark.studentId())) {
                throw new BadRequestException("Student " + mark.studentId() + " is not in your class.");
            }
            Attendance attendance = attendanceRepository
                    .findByStudentIdAndAttendanceDateAndSession(mark.studentId(), request.date(), request.session())
                    .orElseGet(Attendance::new);

            attendance.setSchoolId(tc.schoolId());
            attendance.setStudentId(mark.studentId());
            attendance.setClassLabel(tc.classLabel());
            attendance.setAttendanceDate(request.date());
            attendance.setSession(request.session());
            attendance.setStatus(mark.status());
            attendance.setMarkedByTeacherId(tc.teacherId());
            attendance.setMarkedAt(now);
            attendanceRepository.save(attendance);

            if (mark.status() == AttendanceStatus.ABSENT) {
                absentees.add(new AbsenteeNotification(
                        mark.studentId(), tc.schoolId(), tc.classLabel(), request.date(), request.session().name()));
            }
        }

        notifyAbsentees(absentees);
        return buildDaySheet(tc.schoolId(), tc.classLabel(), request.date(), request.session(), roster);
    }

    @Override
    @Transactional
    public void correctMark(UUID teacherUserId, UUID attendanceId, AttendanceStatus status) {
        TeacherClassView tc = access.requireTeacherClass(teacherUserId);
        Attendance attendance = attendanceRepository.findByIdAndSchoolId(attendanceId, tc.schoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found."));
        if (!attendance.getClassLabel().equals(tc.classLabel())) {
            throw new ForbiddenException("That attendance record is not for your class.");
        }
        attendance.setStatus(status);
        attendance.setMarkedByTeacherId(tc.teacherId());
        attendance.setMarkedAt(LocalDateTime.now());
        attendanceRepository.save(attendance);

        if (status == AttendanceStatus.ABSENT) {
            notifyAbsentees(List.of(new AbsenteeNotification(
                    attendance.getStudentId(), attendance.getSchoolId(), attendance.getClassLabel(),
                    attendance.getAttendanceDate(), attendance.getSession().name())));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceDaySheet getDaySheet(UUID teacherUserId, LocalDate date, AttendanceSession session) {
        TeacherClassView tc = access.requireTeacherClass(teacherUserId);
        List<RosterStudent> roster = access.roster(tc.schoolId(), tc.classLabel());
        return buildDaySheet(tc.schoolId(), tc.classLabel(), date, session, roster);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbsenteeEntry> getTodayAbsentees(UUID schoolId) {
        LocalDate today = LocalDate.now();
        List<Attendance> absents =
                attendanceRepository.findBySchoolIdAndAttendanceDateAndStatus(schoolId, today, AttendanceStatus.ABSENT);

        Map<String, Map<UUID, String>> nameCache = new HashMap<>();
        List<AbsenteeEntry> result = new ArrayList<>();
        for (Attendance a : absents) {
            Map<UUID, String> names = nameCache.computeIfAbsent(a.getClassLabel(),
                    label -> access.roster(schoolId, label).stream()
                            .collect(Collectors.toMap(RosterStudent::studentId, RosterStudent::fullName)));
            result.add(new AbsenteeEntry(a.getStudentId(), names.get(a.getStudentId()), a.getClassLabel(), a.getSession()));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassAttendanceSummary> getSummary(UUID schoolId, LocalDate from, LocalDate to) {
        Map<String, long[]> counts = new LinkedHashMap<>(); // [present, absent, leave]
        for (Object[] row : attendanceRepository.summarizeByClass(schoolId, from, to)) {
            String classLabel = (String) row[0];
            AttendanceStatus status = (AttendanceStatus) row[1];
            long count = ((Number) row[2]).longValue();
            long[] c = counts.computeIfAbsent(classLabel, k -> new long[3]);
            switch (status) {
                case PRESENT -> c[0] += count;
                case ABSENT -> c[1] += count;
                case LEAVE -> c[2] += count;
            }
        }
        return counts.entrySet().stream().map(e -> {
            long present = e.getValue()[0];
            long absent = e.getValue()[1];
            long leave = e.getValue()[2];
            long total = present + absent + leave;
            double pct = total == 0 ? 0.0 : present * 100.0 / total;
            return new ClassAttendanceSummary(e.getKey(), present, absent, leave, total, pct);
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentAttendanceView getStudentAttendance(UUID parentUserId, UUID studentId, String month) {
        access.requireOwnedChild(parentUserId, studentId);

        YearMonth ym = parseMonth(month);
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        List<Attendance> rows = attendanceRepository
                .findByStudentIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(studentId, from, to);

        long present = rows.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        long absent = rows.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        long leave = rows.stream().filter(a -> a.getStatus() == AttendanceStatus.LEAVE).count();
        long total = rows.size();
        double pct = total == 0 ? 0.0 : present * 100.0 / total;

        List<AttendanceDayMark> daily = rows.stream().map(AcademicMappers::toAttendanceDayMark).toList();
        return new StudentAttendanceView(studentId, ym.toString(), daily, present, absent, leave, total, pct);
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolAttendanceStats getSchoolStats(UUID schoolId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            YearMonth ym = YearMonth.now();
            from = ym.atDay(1);
            to = ym.atEndOfMonth();
        }
        long present = 0;
        long absent = 0;
        long leave = 0;
        for (Object[] row : attendanceRepository.summarizeBySchool(schoolId, from, to)) {
            AttendanceStatus status = (AttendanceStatus) row[0];
            long count = ((Number) row[1]).longValue();
            switch (status) {
                case PRESENT -> present += count;
                case ABSENT -> absent += count;
                case LEAVE -> leave += count;
            }
        }
        long total = present + absent + leave;
        double pct = total == 0 ? 0.0 : present * 100.0 / total;
        return new SchoolAttendanceStats(schoolId, present, absent, leave, total, pct);
    }

    // ---------------- helpers ----------------

    private AttendanceDaySheet buildDaySheet(UUID schoolId, String classLabel, LocalDate date,
                                             AttendanceSession session, List<RosterStudent> roster) {
        Map<UUID, AttendanceStatus> marked = attendanceRepository
                .findBySchoolIdAndClassLabelAndAttendanceDateAndSession(schoolId, classLabel, date, session).stream()
                .collect(Collectors.toMap(Attendance::getStudentId, Attendance::getStatus));

        List<AttendanceEntry> entries = roster.stream()
                .map(s -> new AttendanceEntry(s.studentId(), s.admissionNo(), s.fullName(), marked.get(s.studentId())))
                .toList();
        return new AttendanceDaySheet(classLabel, date, session, entries);
    }

    private YearMonth parseMonth(String month) {
        if (month == null || month.isBlank()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("month must be in YYYY-MM format");
        }
    }

    private void notifyAbsentees(List<AbsenteeNotification> absentees) {
        if (absentees.isEmpty()) {
            return;
        }
        try {
            notificationClient.notifyAbsentees(absentees);
        } catch (Exception ex) {
            log.warn("Failed to dispatch {} absentee notification(s): {}", absentees.size(), ex.getMessage());
        }
    }
}
