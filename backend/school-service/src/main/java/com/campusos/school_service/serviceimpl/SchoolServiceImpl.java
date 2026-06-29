package com.campusos.school_service.serviceimpl;

import com.campusos.common_lib.contract.SchoolAttendanceStats;
import com.campusos.common_lib.contract.SchoolFeeStats;
import com.campusos.common_lib.contract.SchoolSummary;
import com.campusos.school_service.client.AcademicStatsClient;
import com.campusos.school_service.client.AuthClient;
import com.campusos.school_service.client.FeeStatsClient;
import com.campusos.school_service.client.dto.AuthAdminRequest;
import com.campusos.school_service.client.dto.AuthUserResponse;
import com.campusos.school_service.dto.request.CreateSchoolAdminRequest;
import com.campusos.school_service.dto.request.CreateSchoolRequest;
import com.campusos.school_service.dto.request.UpdateSchoolRequest;
import com.campusos.school_service.dto.response.SchoolAdminResponse;
import com.campusos.school_service.dto.response.SchoolOverview;
import com.campusos.school_service.dto.response.SchoolReport;
import com.campusos.school_service.dto.response.SchoolResponse;
import com.campusos.school_service.entity.School;
import com.campusos.school_service.entity.SchoolAdmin;
import com.campusos.school_service.exception.DuplicateResourceException;
import com.campusos.school_service.exception.ResourceNotFoundException;
import com.campusos.school_service.exception.ServiceUnavailableException;
import com.campusos.school_service.mapper.SchoolMappers;
import com.campusos.school_service.repository.ClassTeacherRepository;
import com.campusos.school_service.repository.SchoolAdminRepository;
import com.campusos.school_service.repository.SchoolRepository;
import com.campusos.school_service.repository.StudentRepository;
import com.campusos.school_service.repository.TeacherRepository;
import com.campusos.school_service.service.SchoolService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;
    private final SchoolAdminRepository schoolAdminRepository;
    private final TeacherRepository teacherRepository;
    private final ClassTeacherRepository classTeacherRepository;
    private final StudentRepository studentRepository;
    private final AuthClient authClient;
    private final AcademicStatsClient academicStatsClient;
    private final FeeStatsClient feeStatsClient;

    @Override
    @Transactional
    public SchoolResponse createSchool(CreateSchoolRequest request) {
        String code = request.code().trim().toUpperCase();
        if (schoolRepository.existsByCode(code)) {
            throw new DuplicateResourceException("A school with code " + code + " already exists.");
        }
        School school = School.builder()
                .name(request.name())
                .code(code)
                .address(request.address())
                .village(request.village())
                .district(request.district())
                .state(request.state())
                .pincode(request.pincode())
                .phone(request.phone())
                .email(request.email())
                .active(true)
                .build();
        return SchoolMappers.toSchoolResponse(schoolRepository.save(school));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolResponse> listSchools() {
        return schoolRepository.findAll().stream().map(SchoolMappers::toSchoolResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolResponse getSchool(UUID id) {
        return SchoolMappers.toSchoolResponse(findSchool(id));
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolSummary getSchoolSummary(UUID id) {
        return SchoolMappers.toSchoolSummary(findSchool(id));
    }

    @Override
    @Transactional
    public SchoolResponse updateSchool(UUID id, UpdateSchoolRequest request) {
        School school = findSchool(id);
        if (request.name() != null) school.setName(request.name());
        if (request.address() != null) school.setAddress(request.address());
        if (request.village() != null) school.setVillage(request.village());
        if (request.district() != null) school.setDistrict(request.district());
        if (request.state() != null) school.setState(request.state());
        if (request.pincode() != null) school.setPincode(request.pincode());
        if (request.phone() != null) school.setPhone(request.phone());
        if (request.email() != null) school.setEmail(request.email());
        if (request.active() != null) school.setActive(request.active());
        return SchoolMappers.toSchoolResponse(schoolRepository.save(school));
    }

    @Override
    @Transactional
    public SchoolResponse updateLogo(UUID id, String logoUrl) {
        School school = findSchool(id);
        school.setLogoUrl(logoUrl);
        return SchoolMappers.toSchoolResponse(schoolRepository.save(school));
    }

    @Override
    @Transactional
    public SchoolAdminResponse createSchoolAdmin(UUID schoolId, CreateSchoolAdminRequest request) {
        findSchool(schoolId); // validate school exists
        if (schoolAdminRepository.existsBySchoolId(schoolId)) {
            throw new DuplicateResourceException("This school already has an admin.");
        }

        // Provision the login in auth-service first; if it fails, nothing is persisted here.
        AuthUserResponse user;
        try {
            user = authClient.createAdmin(new AuthAdminRequest(
                    request.fullName(), request.email(), request.phone(), request.password(), schoolId));
        } catch (FeignException.Conflict ex) {
            throw new DuplicateResourceException("That email is already in use.");
        } catch (FeignException ex) {
            log.warn("auth-service rejected admin creation ({}): {}", ex.status(), ex.getMessage());
            throw new ServiceUnavailableException("Auth service is unavailable. Please try again later.");
        }

        SchoolAdmin admin = SchoolAdmin.builder()
                .schoolId(schoolId)
                .userId(user.id())
                .fullName(request.fullName())
                .phone(request.phone())
                .build();
        return SchoolMappers.toAdminResponse(schoolAdminRepository.save(admin));
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolAdminResponse getSchoolAdmin(UUID schoolId) {
        SchoolAdmin admin = schoolAdminRepository.findBySchoolId(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("This school has no admin yet."));
        return SchoolMappers.toAdminResponse(admin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolOverview> overview() {
        return schoolRepository.findAll().stream()
                .map(s -> new SchoolOverview(
                        s.getId(), s.getName(), s.getCode(),
                        studentRepository.countBySchoolId(s.getId()),
                        teacherRepository.countBySchoolId(s.getId()),
                        classTeacherRepository.countBySchoolId(s.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolReport> reports(LocalDate from, LocalDate to) {
        return schoolRepository.findAll().stream()
                .map(s -> {
                    SchoolAttendanceStats att = safeAttendance(s.getId(), from, to);
                    SchoolFeeStats fee = safeFee(s.getId());
                    return new SchoolReport(
                            s.getId(), s.getName(), s.getCode(),
                            studentRepository.countBySchoolId(s.getId()),
                            teacherRepository.countBySchoolId(s.getId()),
                            classTeacherRepository.countBySchoolId(s.getId()),
                            att != null ? att.presentPercentage() : null,
                            fee != null ? fee.collectionPercentage() : null,
                            fee != null ? fee.paidTotal() : null,
                            fee != null ? fee.pendingTotal() : null);
                })
                .toList();
    }

    private SchoolAttendanceStats safeAttendance(UUID schoolId, LocalDate from, LocalDate to) {
        try {
            return academicStatsClient.getSchoolStats(schoolId, from, to);
        } catch (Exception ex) {
            log.warn("academic-service unavailable for attendance stats of {}: {}", schoolId, ex.getMessage());
            return null;
        }
    }

    private SchoolFeeStats safeFee(UUID schoolId) {
        try {
            return feeStatsClient.getSchoolStats(schoolId);
        } catch (Exception ex) {
            log.warn("fee-service unavailable for fee stats of {}: {}", schoolId, ex.getMessage());
            return null;
        }
    }

    private School findSchool(UUID id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School " + id + " not found."));
    }
}
