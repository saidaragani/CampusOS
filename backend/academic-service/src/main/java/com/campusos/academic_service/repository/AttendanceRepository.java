package com.campusos.academic_service.repository;

import com.campusos.academic_service.entity.Attendance;
import com.campusos.academic_service.enums.AttendanceSession;
import com.campusos.academic_service.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    Optional<Attendance> findByStudentIdAndAttendanceDateAndSession(
            UUID studentId, LocalDate attendanceDate, AttendanceSession session);

    Optional<Attendance> findByIdAndSchoolId(UUID id, UUID schoolId);

    List<Attendance> findBySchoolIdAndClassLabelAndAttendanceDateAndSession(
            UUID schoolId, String classLabel, LocalDate attendanceDate, AttendanceSession session);

    List<Attendance> findBySchoolIdAndAttendanceDateAndStatus(
            UUID schoolId, LocalDate attendanceDate, AttendanceStatus status);

    List<Attendance> findByStudentIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
            UUID studentId, LocalDate from, LocalDate to);

    /** Rows: [classLabel(String), status(AttendanceStatus), count(Long)]. */
    @Query("select a.classLabel, a.status, count(a) from Attendance a "
            + "where a.schoolId = :schoolId and a.attendanceDate between :from and :to "
            + "group by a.classLabel, a.status")
    List<Object[]> summarizeByClass(@Param("schoolId") UUID schoolId,
                                    @Param("from") LocalDate from,
                                    @Param("to") LocalDate to);

    /** Rows: [status(AttendanceStatus), count(Long)] across the whole school. */
    @Query("select a.status, count(a) from Attendance a "
            + "where a.schoolId = :schoolId and a.attendanceDate between :from and :to "
            + "group by a.status")
    List<Object[]> summarizeBySchool(@Param("schoolId") UUID schoolId,
                                     @Param("from") LocalDate from,
                                     @Param("to") LocalDate to);
}
