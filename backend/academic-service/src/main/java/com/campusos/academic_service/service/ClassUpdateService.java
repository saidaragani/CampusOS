package com.campusos.academic_service.service;

import com.campusos.academic_service.dto.request.PostClassUpdateRequest;
import com.campusos.academic_service.dto.response.ClassUpdateResponse;

import java.util.List;
import java.util.UUID;

public interface ClassUpdateService {

    ClassUpdateResponse postUpdate(UUID teacherUserId, PostClassUpdateRequest request);

    List<ClassUpdateResponse> getClassFeed(UUID schoolId, String classLabel);

    void deleteUpdate(UUID teacherUserId, UUID id);
}
