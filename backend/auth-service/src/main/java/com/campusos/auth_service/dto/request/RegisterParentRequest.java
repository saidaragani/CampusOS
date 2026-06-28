package com.campusos.auth_service.dto.request;

import com.campusos.common_lib.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RegisterParentRequest(
    @NotBlank
    String fullName,

    @Email
    @NotBlank
    String email,

    @StrongPassword
    String password,

    @NotNull
    UUID schoolId,

    @NotBlank
    String admissionNumber
) {}
