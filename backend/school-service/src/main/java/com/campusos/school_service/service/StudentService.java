package com.campusos.school_service.service;

import com.campusos.common_lib.contract.RosterStudent;
import com.campusos.common_lib.contract.StudentSummary;
import com.campusos.school_service.dto.request.CreateStudentRequest;
import com.campusos.school_service.dto.request.UpdateStudentRequest;
import com.campusos.school_service.dto.response.StudentContactDto;
import com.campusos.school_service.dto.response.StudentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface StudentService {

    StudentResponse createStudent(UUID schoolId, CreateStudentRequest request);

    Page<StudentResponse> listStudents(UUID schoolId, String classLabel, Pageable pageable);

    StudentResponse getStudent(UUID schoolId, UUID id);

    StudentResponse updateStudent(UUID schoolId, UUID id, UpdateStudentRequest request);

    void deactivateStudent(UUID schoolId, UUID id);

    StudentResponse updatePhoto(UUID schoolId, UUID id, String photoUrl);

    // --- internal (service-to-service) ---

    StudentSummary lookupByAdmission(UUID schoolId, String admissionNo);

    List<RosterStudent> roster(UUID schoolId, String classLabel);

    List<StudentContactDto> byClass(UUID classId);

    List<StudentContactDto> birthdays(LocalDate date);
}
