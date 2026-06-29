package com.campusos.academic_service.dto.response;

import java.util.List;

public record TimetableResponse(
        String classLabel,
        List<TimetableSlotResponse> slots
) {}
