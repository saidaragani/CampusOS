package com.campusos.auth_service.dto.request;

public record ChildDto(
        Long studentId, Long schoolId, String admissionNo
) {}