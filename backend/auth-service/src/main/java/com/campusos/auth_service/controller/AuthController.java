package com.campusos.auth_service.controller;

import com.campusos.auth_service.dto.request.*;
import com.campusos.auth_service.dto.response.AuthResponse;
import com.campusos.auth_service.dto.response.UserSummaryDto;
import com.campusos.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/parent")
    public ResponseEntity<Void> signUpParent(@RequestBody ParentSignUpRequest request) {
        authService.signUpParent(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok().build(); // Always 200 for security reasons
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request, Principal principal) {
        authService.changePassword(request, principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSummaryDto> getCurrentUser(Principal principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal.getName()));
    }

    @PostMapping("/children/link")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<Void> linkChild(@RequestBody LinkChildRequest request, Principal principal) {
        authService.linkChild(request, principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/children")
    @PreAuthorize("hasRole('PARENT')")
    public ResponseEntity<List<ChildDto>> getChildren(Principal principal) {
        return ResponseEntity.ok(authService.getChildren(principal.getName()));
    }
}