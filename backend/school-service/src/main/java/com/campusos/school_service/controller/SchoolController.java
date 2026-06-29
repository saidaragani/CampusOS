package com.campusos.school_service.controller;

import com.campusos.school_service.dto.request.CreateSchoolAdminRequest;
import com.campusos.school_service.dto.request.CreateSchoolRequest;
import com.campusos.school_service.dto.request.LogoRequest;
import com.campusos.school_service.dto.request.UpdateSchoolRequest;
import com.campusos.school_service.dto.response.SchoolAdminResponse;
import com.campusos.school_service.dto.response.SchoolOverview;
import com.campusos.school_service.dto.response.SchoolReport;
import com.campusos.school_service.dto.response.SchoolResponse;
import com.campusos.school_service.security.AuthenticatedUser;
import com.campusos.school_service.service.SchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SchoolResponse> create(@Valid @RequestBody CreateSchoolRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schoolService.createSchool(request));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<SchoolResponse>> list() {
        return ResponseEntity.ok(schoolService.listSchools());
    }

    @GetMapping("/overview")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<SchoolOverview>> overview() {
        return ResponseEntity.ok(schoolService.overview());
    }

    /** Cross-school report: counts + attendance-% + fee-collection-% per school. */
    @GetMapping("/reports")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<SchoolReport>> reports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(schoolService.reports(from, to));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<SchoolResponse> get(@PathVariable UUID id,
                                              @AuthenticationPrincipal AuthenticatedUser user) {
        assertSchoolAccess(user, id);
        return ResponseEntity.ok(schoolService.getSchool(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<SchoolResponse> update(@PathVariable UUID id,
                                                 @Valid @RequestBody UpdateSchoolRequest request,
                                                 @AuthenticationPrincipal AuthenticatedUser user) {
        assertSchoolAccess(user, id);
        return ResponseEntity.ok(schoolService.updateSchool(id, request));
    }

    @PostMapping("/{id}/logo")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<SchoolResponse> updateLogo(@PathVariable UUID id,
                                                     @Valid @RequestBody LogoRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser user) {
        assertSchoolAccess(user, id);
        return ResponseEntity.ok(schoolService.updateLogo(id, request.logoUrl()));
    }

    @PostMapping("/{id}/admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SchoolAdminResponse> createAdmin(@PathVariable UUID id,
                                                           @Valid @RequestBody CreateSchoolAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(schoolService.createSchoolAdmin(id, request));
    }

    @GetMapping("/{id}/admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SchoolAdminResponse> getAdmin(@PathVariable UUID id) {
        return ResponseEntity.ok(schoolService.getSchoolAdmin(id));
    }

    /** An ADMIN may only touch their own school; SUPER_ADMIN may touch any. */
    private void assertSchoolAccess(AuthenticatedUser user, UUID schoolId) {
        if (!"SUPER_ADMIN".equals(user.role()) && !schoolId.equals(user.schoolId())) {
            throw new AccessDeniedException("You can only access your own school.");
        }
    }
}
