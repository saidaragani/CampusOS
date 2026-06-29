package com.campusos.school_service.controller;

import com.campusos.school_service.dto.request.CreateTeacherRequest;
import com.campusos.school_service.dto.request.UpdateTeacherRequest;
import com.campusos.school_service.dto.response.ClassResponse;
import com.campusos.school_service.dto.response.RosterEntry;
import com.campusos.school_service.dto.response.TeacherResponse;
import com.campusos.school_service.security.AuthenticatedUser;
import com.campusos.school_service.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> create(@Valid @RequestBody CreateTeacherRequest request,
                                                  @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherService.createTeacher(user.schoolId(), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<TeacherResponse>> list(@PageableDefault(size = 20) Pageable pageable,
                                                      @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(teacherService.listTeachers(user.schoolId(), pageable));
    }

    @GetMapping("/me/class")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ClassResponse> myClass(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(teacherService.getMyClass(user.userId(), user.schoolId()));
    }

    @GetMapping("/me/roster")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<RosterEntry>> myRoster(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(teacherService.getMyRoster(user.userId(), user.schoolId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> get(@PathVariable UUID id,
                                               @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(teacherService.getTeacher(user.schoolId(), id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateTeacherRequest request,
                                                  @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(teacherService.updateTeacher(user.schoolId(), id, request));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id,
                                           @AuthenticationPrincipal AuthenticatedUser user) {
        teacherService.deactivateTeacher(user.schoolId(), id);
        return ResponseEntity.noContent().build();
    }
}
