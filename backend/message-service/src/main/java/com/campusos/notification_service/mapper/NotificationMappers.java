package com.campusos.notification_service.mapper;

import com.campusos.notification_service.dto.response.NotificationLogResponse;
import com.campusos.notification_service.dto.response.WishResponse;
import com.campusos.notification_service.entity.NotificationLog;
import com.campusos.notification_service.entity.WishSchedule;

public final class NotificationMappers {

    private NotificationMappers() {
    }

    public static WishResponse toWishResponse(WishSchedule w) {
        return new WishResponse(w.getId(), w.getSchoolId(), w.getFestivalName(), w.getMessage(),
                w.getScheduledDate(), w.getAudience(), w.isSent());
    }

    public static NotificationLogResponse toLogResponse(NotificationLog l) {
        return new NotificationLogResponse(l.getId(), l.getSchoolId(), l.getChannel(), l.getRecipientEmail(),
                l.getSubject(), l.getTemplate(), l.getStatus(), l.getErrorMessage(), l.getCreatedAt(), l.getSentAt());
    }
}
