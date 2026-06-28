package com.campusos.auth_service.dto.request;

import com.campusos.common_lib.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
    @NotBlank
    String oldPassword,

    @StrongPassword
    String newPassword
) {}
