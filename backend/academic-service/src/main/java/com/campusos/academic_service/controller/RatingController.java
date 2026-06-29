package com.campusos.academic_service.controller;

import com.campusos.academic_service.dto.request.SubmitRatingRequest;
import com.campusos.academic_service.dto.response.RatingResponse;
import com.campusos.academic_service.security.AuthenticatedUser;
import com.campusos.academic_service.service.RatingService;
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
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<RatingResponse> submit(@Valid @RequestBody SubmitRatingRequest request,
                                                 @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.submitRating(user.userId(), request));
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<RatingResponse>> classRatings(@RequestParam String month,
                                                             @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ratingService.getClassRatings(user.userId(), month));
    }

    @GetMapping("/student/{id}")
    @PreAuthorize("hasAnyRole('PARENT','ADMIN')")
    public ResponseEntity<List<RatingResponse>> studentRatings(@PathVariable UUID id,
                                                               @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(ratingService.getStudentRatings(user.userId(), user.role(), user.schoolId(), id));
    }
}
