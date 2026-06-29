package com.campusos.school_service.controller;

import com.campusos.school_service.dto.request.AssignTeacherRequest;
import com.campusos.school_service.dto.request.CreateClassRequest;
import com.campusos.school_service.dto.request.UpdateClassRequest;
import com.campusos.school_service.dto.response.ClassResponse;
import com.campusos.school_service.security.AuthenticatedUser;
import com.campusos.school_service.service.ClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassResponse> create(@Valid @RequestBody CreateClassRequest request,
                                                @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classService.createClass(user.schoolId(), request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<ClassResponse>> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(classService.listClasses(user.schoolId()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<ClassResponse> get(@PathVariable UUID id,
                                             @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(classService.getClass(user.schoolId(), id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassResponse> update(@PathVariable UUID id,
                                                @RequestBody UpdateClassRequest request,
                                                @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(classService.updateClass(user.schoolId(), id, request.academicYear()));
    }

    @PutMapping("/{id}/teacher")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClassResponse> assignTeacher(@PathVariable UUID id,
                                                       @Valid @RequestBody AssignTeacherRequest request,
                                                       @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(classService.assignTeacher(user.schoolId(), id, request.teacherId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal AuthenticatedUser user) {
        classService.deleteClass(user.schoolId(), id);
        return ResponseEntity.noContent().build();
    }
}
