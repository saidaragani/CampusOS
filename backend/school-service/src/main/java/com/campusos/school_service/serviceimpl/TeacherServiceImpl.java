package com.campusos.school_service.serviceimpl;

import com.campusos.common_lib.contract.RecipientContact;
import com.campusos.common_lib.contract.TeacherClassView;
import com.campusos.school_service.client.AuthClient;
import com.campusos.school_service.client.dto.AuthTeacherRequest;
import com.campusos.school_service.client.dto.AuthUserResponse;
import com.campusos.school_service.dto.request.CreateTeacherRequest;
import com.campusos.school_service.dto.request.UpdateTeacherRequest;
import com.campusos.school_service.dto.response.ClassResponse;
import com.campusos.school_service.dto.response.RosterEntry;
import com.campusos.school_service.dto.response.TeacherContactDto;
import com.campusos.school_service.dto.response.TeacherResponse;
import com.campusos.school_service.entity.ClassTeacher;
import com.campusos.school_service.entity.Teacher;
import com.campusos.school_service.exception.DuplicateResourceException;
import com.campusos.school_service.exception.ResourceNotFoundException;
import com.campusos.school_service.exception.ServiceUnavailableException;
import com.campusos.school_service.mapper.SchoolMappers;
import com.campusos.school_service.repository.ClassTeacherRepository;
import com.campusos.school_service.repository.StudentRepository;
import com.campusos.school_service.repository.TeacherRepository;
import com.campusos.school_service.service.TeacherService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final ClassTeacherRepository classTeacherRepository;
    private final StudentRepository studentRepository;
    private final AuthClient authClient;

    @Override
    @Transactional
    public TeacherResponse createTeacher(UUID schoolId, CreateTeacherRequest request) {
        // Create the domain record first to obtain its id, then provision the login.
        Teacher teacher = teacherRepository.saveAndFlush(Teacher.builder()
                .schoolId(schoolId)
                .fullName(request.fullName())
                .email(request.email())
                .phone(request.phone())
                .qualification(request.qualification())
                .salary(request.salary())
                .active(true)
                .build());

        AuthUserResponse user;
        try {
            user = authClient.createTeacher(new AuthTeacherRequest(
                    request.fullName(), request.email(), request.phone(), request.password(), teacher.getId()));
        } catch (FeignException.Conflict ex) {
            throw new DuplicateResourceException("That email is already in use.");
        } catch (FeignException ex) {
            log.warn("auth-service rejected teacher creation ({}): {}", ex.status(), ex.getMessage());
            throw new ServiceUnavailableException("Auth service is unavailable. Please try again later.");
        }

        teacher.setUserId(user.id());
        return SchoolMappers.toTeacherResponse(teacherRepository.save(teacher));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherResponse> listTeachers(UUID schoolId, Pageable pageable) {
        return teacherRepository.findBySchoolId(schoolId, pageable).map(SchoolMappers::toTeacherResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherResponse getTeacher(UUID schoolId, UUID id) {
        return SchoolMappers.toTeacherResponse(requireTeacher(schoolId, id));
    }

    @Override
    @Transactional
    public TeacherResponse updateTeacher(UUID schoolId, UUID id, UpdateTeacherRequest request) {
        Teacher teacher = requireTeacher(schoolId, id);
        if (request.fullName() != null) teacher.setFullName(request.fullName());
        if (request.phone() != null) teacher.setPhone(request.phone());
        if (request.qualification() != null) teacher.setQualification(request.qualification());
        if (request.salary() != null) teacher.setSalary(request.salary());
        if (request.active() != null) teacher.setActive(request.active());
        return SchoolMappers.toTeacherResponse(teacherRepository.save(teacher));
    }

    @Override
    @Transactional
    public void deactivateTeacher(UUID schoolId, UUID id) {
        Teacher teacher = requireTeacher(schoolId, id);
        teacher.setActive(false);
        teacherRepository.save(teacher);
    }

    @Override
    @Transactional(readOnly = true)
    public ClassResponse getMyClass(UUID userId, UUID schoolId) {
        Teacher teacher = requireTeacherByUser(userId, schoolId);
        ClassTeacher c = classTeacherRepository.findFirstByTeacherId(teacher.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You are not assigned to a class yet."));
        return SchoolMappers.toClassResponse(c, teacher.getFullName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RosterEntry> getMyRoster(UUID userId, UUID schoolId) {
        Teacher teacher = requireTeacherByUser(userId, schoolId);
        ClassTeacher c = classTeacherRepository.findFirstByTeacherId(teacher.getId())
                .orElseThrow(() -> new ResourceNotFoundException("You are not assigned to a class yet."));
        return studentRepository.findBySchoolIdAndClassLabelAndActiveTrue(schoolId, c.getClassLabel()).stream()
                .map(SchoolMappers::toRosterEntry)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherContactDto getContact(UUID id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher " + id + " not found."));
        return SchoolMappers.toTeacherContact(teacher);
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherClassView getClassViewByUser(UUID userId) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No teacher record for user " + userId + "."));
        String classLabel = classTeacherRepository.findFirstByTeacherId(teacher.getId())
                .map(ClassTeacher::getClassLabel)
                .orElse(null);
        return new TeacherClassView(teacher.getId(), teacher.getSchoolId(), classLabel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipientContact> getTeacherRecipients(UUID schoolId) {
        return teacherRepository.findBySchoolIdAndActiveTrue(schoolId).stream()
                .filter(t -> t.getEmail() != null && !t.getEmail().isBlank())
                .map(t -> new RecipientContact(t.getEmail(), t.getFullName()))
                .toList();
    }

    private Teacher requireTeacher(UUID schoolId, UUID id) {
        return teacherRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher " + id + " not found in this school."));
    }

    private Teacher requireTeacherByUser(UUID userId, UUID schoolId) {
        Teacher teacher = teacherRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No teacher record for the current user."));
        if (!teacher.getSchoolId().equals(schoolId)) {
            throw new ResourceNotFoundException("No teacher record for the current user.");
        }
        return teacher;
    }
}
