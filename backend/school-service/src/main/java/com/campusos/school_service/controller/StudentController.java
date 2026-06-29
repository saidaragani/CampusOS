package com.campusos.school_service.controller;

import com.campusos.school_service.dto.request.CreateStudentRequest;
import com.campusos.school_service.dto.request.PhotoRequest;
import com.campusos.school_service.dto.request.UpdateStudentRequest;
import com.campusos.school_service.dto.response.StudentResponse;
import com.campusos.school_service.security.AuthenticatedUser;
import com.campusos.school_service.service.StudentService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> create(@Valid @RequestBody CreateStudentRequest request,
                                                  @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(user.schoolId(), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<StudentResponse>> list(@RequestParam(required = false) String classLabel,
                                                      @PageableDefault(size = 20) Pageable pageable,
                                                      @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(studentService.listStudents(user.schoolId(), classLabel, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<StudentResponse> get(@PathVariable UUID id,
                                               @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(studentService.getStudent(user.schoolId(), id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateStudentRequest request,
                                                  @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(studentService.updateStudent(user.schoolId(), id, request));
    }

    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id,
                                           @AuthenticationPrincipal AuthenticatedUser user) {
        studentService.deactivateStudent(user.schoolId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/photo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponse> updatePhoto(@PathVariable UUID id,
                                                       @Valid @RequestBody PhotoRequest request,
                                                       @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(studentService.updatePhoto(user.schoolId(), id, request.photoUrl()));
    }
}
