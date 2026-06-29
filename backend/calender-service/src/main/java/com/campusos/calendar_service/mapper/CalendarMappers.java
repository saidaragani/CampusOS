package com.campusos.calendar_service.mapper;

import com.campusos.calendar_service.dto.response.AnnouncementResponse;
import com.campusos.calendar_service.dto.response.EventResponse;
import com.campusos.calendar_service.dto.response.GalleryResponse;
import com.campusos.calendar_service.dto.response.HolidayResponse;
import com.campusos.calendar_service.entity.Announcement;
import com.campusos.calendar_service.entity.GalleryItem;
import com.campusos.calendar_service.entity.Holiday;
import com.campusos.calendar_service.entity.SchoolEvent;

public final class CalendarMappers {

    private CalendarMappers() {
    }

    public static HolidayResponse toHolidayResponse(Holiday h) {
        return new HolidayResponse(h.getId(), h.getSchoolId(), h.getName(),
                h.getFromDate(), h.getToDate(), h.getDescription());
    }

    public static EventResponse toEventResponse(SchoolEvent e) {
        return new EventResponse(e.getId(), e.getSchoolId(), e.getTitle(), e.getDescription(),
                e.getEventDate(), e.getEventType());
    }

    public static GalleryResponse toGalleryResponse(GalleryItem g) {
        return new GalleryResponse(g.getId(), g.getSchoolId(), g.getEventId(), g.getTitle(),
                g.getMediaType(), g.getObjectKey(), g.getThumbnailKey(), g.getCreatedAt());
    }

    public static AnnouncementResponse toAnnouncementResponse(Announcement a) {
        return new AnnouncementResponse(a.getId(), a.getSchoolId(), a.getTitle(), a.getBody(),
                a.getAudience(), a.getClassLabel(), a.getAttachmentKey(), a.getPostedByUserId(), a.getCreatedAt());
    }
}
