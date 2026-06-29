package com.campusos.calendar_service.serviceimpl;

import com.campusos.calendar_service.client.NotificationClient;
import com.campusos.calendar_service.client.dto.HolidayNotification;
import com.campusos.calendar_service.dto.request.HolidayRequest;
import com.campusos.calendar_service.dto.response.HolidayResponse;
import com.campusos.calendar_service.entity.Holiday;
import com.campusos.calendar_service.exception.BadRequestException;
import com.campusos.calendar_service.exception.ResourceNotFoundException;
import com.campusos.calendar_service.mapper.CalendarMappers;
import com.campusos.calendar_service.repository.HolidayRepository;
import com.campusos.calendar_service.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayServiceImpl implements HolidayService {

    private final HolidayRepository holidayRepository;
    private final NotificationClient notificationClient;

    @Override
    @Transactional
    public HolidayResponse create(UUID schoolId, UUID userId, HolidayRequest request) {
        if (request.toDate().isBefore(request.fromDate())) {
            throw new BadRequestException("toDate cannot be before fromDate.");
        }
        Holiday holiday = Holiday.builder()
                .schoolId(schoolId)
                .name(request.name())
                .fromDate(request.fromDate())
                .toDate(request.toDate())
                .description(request.description())
                .createdByUserId(userId)
                .build();
        Holiday saved = holidayRepository.save(holiday);

        try {
            notificationClient.notifyHoliday(new HolidayNotification(
                    schoolId, saved.getName(), saved.getFromDate(), saved.getToDate()));
        } catch (Exception ex) {
            log.warn("Failed to publish holiday notification: {}", ex.getMessage());
        }
        return CalendarMappers.toHolidayResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HolidayResponse> list(UUID schoolId, Integer year) {
        List<Holiday> holidays = (year == null)
                ? holidayRepository.findBySchoolIdOrderByFromDateAsc(schoolId)
                : holidayRepository.findBySchoolIdAndFromDateBetweenOrderByFromDateAsc(
                        schoolId, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
        return holidays.stream().map(CalendarMappers::toHolidayResponse).toList();
    }

    @Override
    @Transactional
    public HolidayResponse update(UUID schoolId, UUID id, HolidayRequest request) {
        Holiday holiday = require(schoolId, id);
        holiday.setName(request.name());
        holiday.setFromDate(request.fromDate());
        holiday.setToDate(request.toDate());
        holiday.setDescription(request.description());
        return CalendarMappers.toHolidayResponse(holidayRepository.save(holiday));
    }

    @Override
    @Transactional
    public void delete(UUID schoolId, UUID id) {
        holidayRepository.delete(require(schoolId, id));
    }

    private Holiday require(UUID schoolId, UUID id) {
        return holidayRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Holiday not found."));
    }
}
