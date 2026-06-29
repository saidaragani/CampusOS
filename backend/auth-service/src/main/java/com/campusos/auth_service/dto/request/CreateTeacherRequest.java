package com.campusos.auth_service.dto.request;

import com.campusos.common_lib.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Used by an ADMIN to provision a TEACHER login account. The teacher's domain
 * record (qualification, salary, etc.) lives in the academic/school service and
 * is referenced here by {@code teacherId}. The teacher is bound to the admin's
 * own school (resolved from the authenticated principal, never from the body).
 */
public record CreateTeacherRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        String phone,

        @StrongPassword
        String password,

        @NotNull(message = "Teacher id is required")
        UUID teacherId
) {}
