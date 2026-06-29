package com.campusos.school_service.dto.response;

import java.util.UUID;

public record SchoolResponse(
        UUID id,
        String name,
        String code,
        String address,
        String village,
        String district,
        String state,
        String pincode,
        String phone,
        String email,
        String logoUrl,
        Boolean active
) {}
