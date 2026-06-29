package com.campusos.school_service.service;

import com.campusos.school_service.dto.request.CreateClassRequest;
import com.campusos.school_service.dto.response.ClassResponse;

import java.util.List;
import java.util.UUID;

public interface ClassService {

    ClassResponse createClass(UUID schoolId, CreateClassRequest request);

    List<ClassResponse> listClasses(UUID schoolId);

    ClassResponse getClass(UUID schoolId, UUID id);

    ClassResponse updateClass(UUID schoolId, UUID id, String academicYear);

    ClassResponse assignTeacher(UUID schoolId, UUID id, UUID teacherId);

    void deleteClass(UUID schoolId, UUID id);
}
