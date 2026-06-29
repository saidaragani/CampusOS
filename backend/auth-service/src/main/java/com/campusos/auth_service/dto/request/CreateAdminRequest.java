package com.campusos.auth_service.dto.request;

import com.campusos.common_lib.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Used by a SUPER_ADMIN to provision the single ADMIN account bound to a school.
 * The school record itself lives in school-service; auth-service only owns the
 * login account and the school binding ({@code schoolId}).
 */
public record CreateAdminRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        String phone,

        @StrongPassword
        String password,

        @NotNull(message = "School is required")
        UUID schoolId
) {}
