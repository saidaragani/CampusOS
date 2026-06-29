package com.campusos.academic_service.controller;

import com.campusos.academic_service.dto.request.TimetableRequest;
import com.campusos.academic_service.dto.response.TimetableResponse;
import com.campusos.academic_service.dto.response.TimetableSlotResponse;
import com.campusos.academic_service.security.AuthenticatedUser;
import com.campusos.academic_service.service.TimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TimetableResponse> replace(@Valid @RequestBody TimetableRequest request,
                                                     @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(timetableService.replaceTimetable(user.schoolId(), request));
    }

    @GetMapping("/class/{classLabel}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','PARENT')")
    public ResponseEntity<TimetableResponse> classTimetable(@PathVariable String classLabel,
                                                            @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(timetableService.getClassTimetable(user.schoolId(), classLabel));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<TimetableSlotResponse>> myTimetable(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(timetableService.getMyTimetable(user.userId()));
    }
}
