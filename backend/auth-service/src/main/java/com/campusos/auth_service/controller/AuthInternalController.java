package com.campusos.auth_service.controller;

import com.campusos.auth_service.service.AuthService;
import com.campusos.common_lib.contract.ChildLink;
import com.campusos.common_lib.contract.RecipientContact;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Service-to-service endpoints. NOT routed by the gateway — reachable only inside
 * the cluster (academic/fee/messaging via Feign), so no user JWT.
 */
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class AuthInternalController {

    private final AuthService authService;

    /** A parent's claimed children — used by other services to authorize parent access to a student. */
    @GetMapping("/parents/{userId}/children")
    public ResponseEntity<List<ChildLink>> children(@PathVariable UUID userId) {
        return ResponseEntity.ok(authService.getChildLinksByUserId(userId));
    }

    /** Parent email recipients for a student (messaging service). */
    @GetMapping("/students/{studentId}/parent-recipients")
    public ResponseEntity<List<RecipientContact>> studentParentRecipients(@PathVariable UUID studentId) {
        return ResponseEntity.ok(authService.getStudentParentRecipients(studentId));
    }

    /** All distinct parent email recipients in a school (messaging service). */
    @GetMapping("/schools/{schoolId}/parent-recipients")
    public ResponseEntity<List<RecipientContact>> schoolParentRecipients(@PathVariable UUID schoolId) {
        return ResponseEntity.ok(authService.getSchoolParentRecipients(schoolId));
    }
}
