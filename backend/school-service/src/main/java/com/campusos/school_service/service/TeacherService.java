package com.campusos.school_service.service;

import com.campusos.common_lib.contract.RecipientContact;
import com.campusos.common_lib.contract.TeacherClassView;
import com.campusos.school_service.dto.request.CreateTeacherRequest;
import com.campusos.school_service.dto.request.UpdateTeacherRequest;
import com.campusos.school_service.dto.response.ClassResponse;
import com.campusos.school_service.dto.response.RosterEntry;
import com.campusos.school_service.dto.response.TeacherContactDto;
import com.campusos.school_service.dto.response.TeacherResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TeacherService {

    TeacherResponse createTeacher(UUID schoolId, CreateTeacherRequest request);

    Page<TeacherResponse> listTeachers(UUID schoolId, Pageable pageable);

    TeacherResponse getTeacher(UUID schoolId, UUID id);

    TeacherResponse updateTeacher(UUID schoolId, UUID id, UpdateTeacherRequest request);

    void deactivateTeacher(UUID schoolId, UUID id);

    ClassResponse getMyClass(UUID userId, UUID schoolId);

    List<RosterEntry> getMyRoster(UUID userId, UUID schoolId);

    TeacherContactDto getContact(UUID id);

    /** Resolve a teacher's identity + class assignment from their auth user id. */
    TeacherClassView getClassViewByUser(UUID userId);

    /** Active teachers' email recipients for a school (messaging service). */
    List<RecipientContact> getTeacherRecipients(UUID schoolId);
}
