package com.campusos.auth_service.dto.response;

import java.util.UUID;

/**
 * A child as shown to the parent: identity + class context. The class/section/
 * teacher fields are enriched from student-service and may be null while that
 * service is unavailable (the admission number is always present).
 */
public record ChildView(
        UUID studentId,
        String admissionNo,
        String studentName,
        String classLabel,
        String section,
        String classTeacherName
) {}
