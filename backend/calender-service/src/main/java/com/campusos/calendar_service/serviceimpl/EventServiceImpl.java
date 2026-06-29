package com.campusos.calendar_service.serviceimpl;

import com.campusos.calendar_service.dto.request.EventRequest;
import com.campusos.calendar_service.dto.response.EventResponse;
import com.campusos.calendar_service.entity.SchoolEvent;
import com.campusos.calendar_service.enums.EventType;
import com.campusos.calendar_service.exception.ResourceNotFoundException;
import com.campusos.calendar_service.mapper.CalendarMappers;
import com.campusos.calendar_service.repository.SchoolEventRepository;
import com.campusos.calendar_service.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final SchoolEventRepository eventRepository;

    @Override
    @Transactional
    public EventResponse create(UUID schoolId, UUID userId, EventRequest request) {
        SchoolEvent event = SchoolEvent.builder()
                .schoolId(schoolId)
                .title(request.title())
                .description(request.description())
                .eventDate(request.eventDate())
                .eventType(request.eventType() != null ? request.eventType() : EventType.OTHER)
                .createdByUserId(userId)
                .build();
        return CalendarMappers.toEventResponse(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> list(UUID schoolId, LocalDate from, LocalDate to) {
        List<SchoolEvent> events;
        if (from != null && to != null) {
            events = eventRepository.findBySchoolIdAndEventDateBetweenOrderByEventDateAsc(schoolId, from, to);
        } else {
            events = eventRepository.findBySchoolIdAndEventDateGreaterThanEqualOrderByEventDateAsc(
                    schoolId, LocalDate.now());
        }
        return events.stream().map(CalendarMappers::toEventResponse).toList();
    }

    @Override
    @Transactional
    public EventResponse update(UUID schoolId, UUID id, EventRequest request) {
        SchoolEvent event = require(schoolId, id);
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setEventDate(request.eventDate());
        if (request.eventType() != null) {
            event.setEventType(request.eventType());
        }
        return CalendarMappers.toEventResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public void delete(UUID schoolId, UUID id) {
        eventRepository.delete(require(schoolId, id));
    }

    private SchoolEvent require(UUID schoolId, UUID id) {
        return eventRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found."));
    }
}
