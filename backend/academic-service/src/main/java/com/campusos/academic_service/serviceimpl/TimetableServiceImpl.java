package com.campusos.academic_service.serviceimpl;

import com.campusos.academic_service.dto.request.TimetableRequest;
import com.campusos.academic_service.dto.response.TimetableResponse;
import com.campusos.academic_service.dto.response.TimetableSlotResponse;
import com.campusos.academic_service.entity.TimetableSlot;
import com.campusos.academic_service.mapper.AcademicMappers;
import com.campusos.academic_service.repository.TimetableSlotRepository;
import com.campusos.academic_service.service.TimetableService;
import com.campusos.academic_service.support.AcademicAccessResolver;
import com.campusos.common_lib.contract.TeacherClassView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimetableServiceImpl implements TimetableService {

    private final TimetableSlotRepository timetableSlotRepository;
    private final AcademicAccessResolver access;

    @Override
    @Transactional
    public TimetableResponse replaceTimetable(UUID schoolId, TimetableRequest request) {
        String classLabel = request.classLabel().trim();
        timetableSlotRepository.deleteBySchoolIdAndClassLabel(schoolId, classLabel);
        timetableSlotRepository.flush();

        List<TimetableSlot> slots = request.slots().stream()
                .map(s -> TimetableSlot.builder()
                        .schoolId(schoolId)
                        .classLabel(classLabel)
                        .dayOfWeek(s.dayOfWeek())
                        .periodNo(s.periodNo())
                        .subject(s.subject())
                        .startTime(s.startTime())
                        .endTime(s.endTime())
                        .teacherId(s.teacherId())
                        .build())
                .toList();
        timetableSlotRepository.saveAll(slots);

        return getClassTimetable(schoolId, classLabel);
    }

    @Override
    @Transactional(readOnly = true)
    public TimetableResponse getClassTimetable(UUID schoolId, String classLabel) {
        List<TimetableSlotResponse> slots = timetableSlotRepository
                .findBySchoolIdAndClassLabelOrderByDayOfWeekAscPeriodNoAsc(schoolId, classLabel).stream()
                .map(AcademicMappers::toTimetableSlotResponse)
                .toList();
        return new TimetableResponse(classLabel, slots);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimetableSlotResponse> getMyTimetable(UUID teacherUserId) {
        TeacherClassView tc = access.teacherInfo(teacherUserId);
        return timetableSlotRepository.findByTeacherIdOrderByDayOfWeekAscPeriodNoAsc(tc.teacherId()).stream()
                .map(AcademicMappers::toTimetableSlotResponse)
                .toList();
    }
}
