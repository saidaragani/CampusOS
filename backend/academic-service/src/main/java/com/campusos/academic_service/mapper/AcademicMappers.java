package com.campusos.academic_service.mapper;

import com.campusos.academic_service.dto.response.AttendanceDayMark;
import com.campusos.academic_service.dto.response.ClassUpdateResponse;
import com.campusos.academic_service.dto.response.LeaveResponse;
import com.campusos.academic_service.dto.response.RatingResponse;
import com.campusos.academic_service.dto.response.TimetableSlotResponse;
import com.campusos.academic_service.entity.Attendance;
import com.campusos.academic_service.entity.BehaviourRating;
import com.campusos.academic_service.entity.ClassUpdate;
import com.campusos.academic_service.entity.LeaveRequest;
import com.campusos.academic_service.entity.TimetableSlot;

public final class AcademicMappers {

    private AcademicMappers() {
    }

    public static RatingResponse toRatingResponse(BehaviourRating r) {
        return new RatingResponse(r.getId(), r.getStudentId(), r.getClassLabel(),
                r.getRatingMonth(), r.getBehaviourScore(), r.getRemarks());
    }

    public static TimetableSlotResponse toTimetableSlotResponse(TimetableSlot s) {
        return new TimetableSlotResponse(s.getId(), s.getDayOfWeek(), s.getPeriodNo(), s.getSubject(),
                s.getStartTime(), s.getEndTime(), s.getTeacherId());
    }

    public static LeaveResponse toLeaveResponse(LeaveRequest l) {
        return new LeaveResponse(l.getId(), l.getStudentId(), l.getClassLabel(), l.getFromDate(), l.getToDate(),
                l.getReason(), l.getStatus(), l.getDecisionNote(), l.getDecidedAt());
    }

    public static ClassUpdateResponse toClassUpdateResponse(ClassUpdate c) {
        return new ClassUpdateResponse(c.getId(), c.getClassLabel(), c.getTitle(), c.getBody(),
                c.getType(), c.getPostedByTeacherId(), c.getCreatedAt());
    }

    public static AttendanceDayMark toAttendanceDayMark(Attendance a) {
        return new AttendanceDayMark(a.getAttendanceDate(), a.getSession(), a.getStatus());
    }
}
