package com.campusos.school_service.dto.request;

import com.campusos.common_lib.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CreateTeacherRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        String phone,
        String qualification,

        /** Admin-only field, stored on the teacher record. */
        BigDecimal salary,

        /** Initial password for the teacher's login account. */
        @StrongPassword
        String password
) {}
