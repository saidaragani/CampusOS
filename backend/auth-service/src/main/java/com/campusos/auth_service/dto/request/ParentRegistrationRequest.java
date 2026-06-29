package com.campusos.auth_service.dto.request;

import com.campusos.common_lib.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Parent self-registration. The parent proves ownership of the child with the
 * admission number printed on the admission slip; auth-service validates it
 * against the student-service before creating the account + link.
 */
public record ParentRegistrationRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @StrongPassword
        String password,

        String phone,

        @NotNull(message = "School is required")
        UUID schoolId,

        @NotBlank(message = "Admission number is required")
        String admissionNo
) {}
