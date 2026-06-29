package com.campusos.school_service.dto.request;

import jakarta.validation.constraints.Email;

/** Code is immutable (it is the admission-number prefix), so it is not updatable. */
public record UpdateSchoolRequest(
        String name,
        String address,
        String village,
        String district,
        String state,
        String pincode,
        String phone,

        @Email(message = "Invalid email format")
        String email,

        Boolean active
) {}
