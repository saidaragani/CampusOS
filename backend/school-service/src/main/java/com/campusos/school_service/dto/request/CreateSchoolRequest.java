package com.campusos.school_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateSchoolRequest(
        @NotBlank(message = "School name is required")
        String name,

        @NotBlank(message = "School code is required")
        String code,

        String address,
        String village,
        String district,
        String state,
        String pincode,
        String phone,

        @Email(message = "Invalid email format")
        String email
) {}
