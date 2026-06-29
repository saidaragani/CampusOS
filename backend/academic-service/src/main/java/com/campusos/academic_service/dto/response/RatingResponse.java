package com.campusos.academic_service.dto.response;

import java.util.UUID;

public record RatingResponse(
        UUID id,
        UUID studentId,
        String classLabel,
        String ratingMonth,
        int behaviourScore,
        String remarks
) {}
