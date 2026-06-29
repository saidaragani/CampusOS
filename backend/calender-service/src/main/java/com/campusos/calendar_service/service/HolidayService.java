package com.campusos.calendar_service.service;

import com.campusos.calendar_service.dto.request.HolidayRequest;
import com.campusos.calendar_service.dto.response.HolidayResponse;

import java.util.List;
import java.util.UUID;

public interface HolidayService {

    HolidayResponse create(UUID schoolId, UUID userId, HolidayRequest request);

    List<HolidayResponse> list(UUID schoolId, Integer year);

    HolidayResponse update(UUID schoolId, UUID id, HolidayRequest request);

    void delete(UUID schoolId, UUID id);
}
