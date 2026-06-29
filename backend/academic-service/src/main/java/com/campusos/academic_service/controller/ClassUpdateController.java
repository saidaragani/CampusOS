package com.campusos.academic_service.controller;

import com.campusos.academic_service.dto.request.PostClassUpdateRequest;
import com.campusos.academic_service.dto.response.ClassUpdateResponse;
import com.campusos.academic_service.security.AuthenticatedUser;
import com.campusos.academic_service.service.ClassUpdateService;
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
@RequestMapping("/api/class-updates")
@RequiredArgsConstructor
public class ClassUpdateController {

    private final ClassUpdateService classUpdateService;

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ClassUpdateResponse> post(@Valid @RequestBody PostClassUpdateRequest request,
                                                    @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classUpdateService.postUpdate(user.userId(), request));
    }

    @GetMapping("/class/{classLabel}")
    @PreAuthorize("hasAnyRole('TEACHER','PARENT')")
    public ResponseEntity<List<ClassUpdateResponse>> feed(@PathVariable String classLabel,
                                                          @AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(classUpdateService.getClassFeed(user.schoolId(), classLabel));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal AuthenticatedUser user) {
        classUpdateService.deleteUpdate(user.userId(), id);
        return ResponseEntity.noContent().build();
    }
}
