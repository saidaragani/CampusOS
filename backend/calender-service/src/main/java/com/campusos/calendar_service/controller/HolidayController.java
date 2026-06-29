package com.campusos.calendar_service.controller;

import com.campusos.calendar_service.dto.request.HolidayRequest;
import com.campusos.calendar_service.dto.response.HolidayResponse;
import com.campusos.calendar_service.security.AuthenticatedUser;
import com.campusos.calendar_service.service.HolidayService;
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
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HolidayResponse> create(@Valid @RequestBody HolidayRequest request,
                                                  @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(holidayService.create(user.schoolId(), user.userId(), request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER','PARENT')")
    public ResponseEntity<List<HolidayResponse>> list(@RequestParam(required = false) Integer year,
                                                      @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(holidayService.list(user.schoolId(), year));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HolidayResponse> update(@PathVariable UUID id,
                                                  @Valid @RequestBody HolidayRequest request,
                                                  @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(holidayService.update(user.schoolId(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AuthenticatedUser user) {
        holidayService.delete(user.schoolId(), id);
        return ResponseEntity.noContent().build();
    }
}
