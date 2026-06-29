package com.campusos.academic_service.controller;

import com.campusos.academic_service.service.AttendanceService;
import com.campusos.common_lib.contract.SchoolAttendanceStats;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service-to-service endpoints (NOT routed by the gateway). Used by school-service
 * for cross-school reports.
 */
@RestController
@RequestMapping("/api/internal/attendance")
@RequiredArgsConstructor
public class AcademicInternalController {

    private final AttendanceService attendanceService;

    @GetMapping("/school-stats")
    public ResponseEntity<SchoolAttendanceStats> schoolStats(
            @RequestParam UUID schoolId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(attendanceService.getSchoolStats(schoolId, from, to));
    }
}
