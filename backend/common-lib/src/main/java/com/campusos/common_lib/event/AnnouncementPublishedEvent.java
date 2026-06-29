package com.campusos.common_lib.event;

import java.util.UUID;

/** Published by calendar-service when an announcement is posted. audience = ALL_PARENTS | CLASS | TEACHERS. */
public record AnnouncementPublishedEvent(
        UUID schoolId,
        String audience,
        String classLabel,
        String title,
        String body
) {}
