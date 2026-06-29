package com.campusos.calendar_service.service;

import com.campusos.calendar_service.dto.request.EventRequest;
import com.campusos.calendar_service.dto.response.EventResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EventService {

    EventResponse create(UUID schoolId, UUID userId, EventRequest request);

    /** If both dates are given, filter to that range; otherwise list upcoming events. */
    List<EventResponse> list(UUID schoolId, LocalDate from, LocalDate to);

    EventResponse update(UUID schoolId, UUID id, EventRequest request);

    void delete(UUID schoolId, UUID id);
}
