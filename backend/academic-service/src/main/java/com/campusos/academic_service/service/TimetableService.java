package com.campusos.academic_service.service;

import com.campusos.academic_service.dto.request.TimetableRequest;
import com.campusos.academic_service.dto.response.TimetableResponse;
import com.campusos.academic_service.dto.response.TimetableSlotResponse;

import java.util.List;
import java.util.UUID;

public interface TimetableService {

    TimetableResponse replaceTimetable(UUID schoolId, TimetableRequest request);

    TimetableResponse getClassTimetable(UUID schoolId, String classLabel);

    List<TimetableSlotResponse> getMyTimetable(UUID teacherUserId);
}
