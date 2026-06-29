package com.campusos.fee_service.controller;

import com.campusos.common_lib.contract.SchoolFeeStats;
import com.campusos.fee_service.service.StudentFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Service-to-service endpoints (NOT routed by the gateway). Used by school-service
 * for cross-school reports.
 */
@RestController
@RequestMapping("/api/internal/fees")
@RequiredArgsConstructor
public class FeeInternalController {

    private final StudentFeeService studentFeeService;

    @GetMapping("/school-stats")
    public ResponseEntity<SchoolFeeStats> schoolStats(@RequestParam UUID schoolId) {
        return ResponseEntity.ok(studentFeeService.getSchoolStats(schoolId));
    }
}
