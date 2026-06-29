package com.campusos.academic_service.dto.response;

import com.campusos.academic_service.enums.ClassUpdateType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClassUpdateResponse(
        UUID id,
        String classLabel,
        String title,
        String body,
        ClassUpdateType type,
        UUID postedByTeacherId,
        LocalDateTime createdAt
) {}
