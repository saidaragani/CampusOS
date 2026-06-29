package com.campusos.academic_service.controller;

import com.campusos.academic_service.dto.request.ApplyLeaveRequest;
import com.campusos.academic_service.dto.request.DecideLeaveRequest;
import com.campusos.academic_service.dto.response.LeaveResponse;
import com.campusos.academic_service.security.AuthenticatedUser;
import com.campusos.academic_service.service.LeaveService;
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
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<LeaveResponse> apply(@Valid @RequestBody ApplyLeaveRequest request,
                                               @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leaveService.applyLeave(user.userId(), request));
    }

    @GetMapping("/student/{id}")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<LeaveResponse>> studentLeaves(@PathVariable UUID id,
                                                             @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(leaveService.getStudentLeaves(user.userId(), id));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<LeaveResponse>> pending(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(leaveService.getPendingForTeacher(user.userId()));
    }

    @PutMapping("/{id}/decide")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<LeaveResponse> decide(@PathVariable UUID id,
                                                @Valid @RequestBody DecideLeaveRequest request,
                                                @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(leaveService.decideLeave(user.userId(), id, request));
    }
}
