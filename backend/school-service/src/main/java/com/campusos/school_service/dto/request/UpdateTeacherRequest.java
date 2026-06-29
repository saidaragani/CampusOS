package com.campusos.school_service.dto.request;

import java.math.BigDecimal;

public record UpdateTeacherRequest(
        String fullName,
        String phone,
        String qualification,
        BigDecimal salary,
        Boolean active
) {}
