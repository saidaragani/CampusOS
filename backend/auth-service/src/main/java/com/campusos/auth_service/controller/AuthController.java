package com.campusos.auth_service.controller;

import com.campusos.auth_service.dto.request.*;
import com.campusos.auth_service.dto.response.AuthResponse;
import com.campusos.auth_service.dto.response.ChildView;
import com.campusos.auth_service.dto.response.ParentContextResponse;
import com.campusos.auth_service.dto.response.UserSummaryDto;
import com.campusos.auth_service.security.User.UserPrincipal;
import com.campusos.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // --- Authentication / session ---

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    // --- Account provisioning (the arc) ---

    @PostMapping("/register/parent")
    public ResponseEntity<Void> registerParent(@Valid @RequestBody ParentRegistrationRequest request) {
        authService.registerParent(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/admins")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<UserSummaryDto> createSchoolAdmin(@Valid @RequestBody CreateAdminRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.createSchoolAdmin(request));
    }

    @PostMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserSummaryDto> createTeacher(
            @Valid @RequestBody CreateTeacherRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.createTeacher(request, principal.getSchoolId()));
    }

    // --- Password lifecycle ---

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok().build(); // Always 200 — no account enumeration.
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        authService.changePassword(request, principal.getUsername());
        return ResponseEntity.ok().build();
    }

    // --- Self-service ---

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSummaryDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal.getUsername()));
    }

    /** One-shot payload for the parent portal header + dashboard. */
    @GetMapping("/me/context")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<ParentContextResponse> getParentContext(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.getParentContext(principal.getUsername()));
    }

    @PostMapping("/children/link")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<Void> linkChild(
            @Valid @RequestBody LinkChildRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        authService.linkChild(request, principal.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/children")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<ChildView>> getChildren(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.getChildren(principal.getUsername()));
    }
}
