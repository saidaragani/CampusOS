package com.campusos.school_service.client;

import com.campusos.common_lib.contract.SchoolAttendanceStats;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.UUID;

/** Reads school-wide attendance stats from academic-service (cross-school reports). */
@FeignClient(name = "academic-service", path = "/api/internal/attendance")
public interface AcademicStatsClient {

    @GetMapping("/school-stats")
    SchoolAttendanceStats getSchoolStats(
            @RequestParam("schoolId") UUID schoolId,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to);
}
