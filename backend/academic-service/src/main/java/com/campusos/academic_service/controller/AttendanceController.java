package com.campusos.academic_service.controller;

import com.campusos.academic_service.dto.request.CorrectAttendanceRequest;
import com.campusos.academic_service.dto.request.MarkAttendanceRequest;
import com.campusos.academic_service.dto.response.AbsenteeEntry;
import com.campusos.academic_service.dto.response.AttendanceDaySheet;
import com.campusos.academic_service.dto.response.ClassAttendanceSummary;
import com.campusos.academic_service.dto.response.StudentAttendanceView;
import com.campusos.academic_service.enums.AttendanceSession;
import com.campusos.academic_service.security.AuthenticatedUser;
import com.campusos.academic_service.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AttendanceDaySheet> mark(@Valid @RequestBody MarkAttendanceRequest request,
                                                   @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(attendanceService.markAttendance(user.userId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> correct(@PathVariable UUID id,
                                        @Valid @RequestBody CorrectAttendanceRequest request,
                                        @AuthenticationPrincipal AuthenticatedUser user) {
        attendanceService.correctMark(user.userId(), id, request.status());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<AttendanceDaySheet> daySheet(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam AttendanceSession session,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(attendanceService.getDaySheet(user.userId(), date, session));
    }

    @GetMapping("/today")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AbsenteeEntry>> today(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(attendanceService.getTodayAbsentees(user.schoolId()));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClassAttendanceSummary>> summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(attendanceService.getSummary(user.schoolId(), from, to));
    }

    @GetMapping("/student/{id}")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<StudentAttendanceView> studentAttendance(
            @PathVariable UUID id,
            @RequestParam(required = false) String month,
            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(attendanceService.getStudentAttendance(user.userId(), id, month));
    }
}
