package com.campusos.academic_service.dto.request;

public record DecideLeaveRequest(
        boolean approve,
        String note
) {}
