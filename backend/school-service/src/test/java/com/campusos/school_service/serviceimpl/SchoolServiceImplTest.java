package com.campusos.school_service.serviceimpl;

import com.campusos.school_service.client.AuthClient;
import com.campusos.school_service.client.dto.AuthUserResponse;
import com.campusos.school_service.dto.request.CreateSchoolAdminRequest;
import com.campusos.school_service.dto.request.CreateSchoolRequest;
import com.campusos.school_service.dto.response.SchoolAdminResponse;
import com.campusos.school_service.dto.response.SchoolResponse;
import com.campusos.school_service.entity.School;
import com.campusos.school_service.exception.DuplicateResourceException;
import com.campusos.school_service.repository.ClassTeacherRepository;
import com.campusos.school_service.repository.SchoolAdminRepository;
import com.campusos.school_service.repository.SchoolRepository;
import com.campusos.school_service.repository.StudentRepository;
import com.campusos.school_service.repository.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchoolServiceImplTest {

    @Mock private SchoolRepository schoolRepository;
    @Mock private SchoolAdminRepository schoolAdminRepository;
    @Mock private TeacherRepository teacherRepository;
    @Mock private ClassTeacherRepository classTeacherRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private AuthClient authClient;

    @InjectMocks private SchoolServiceImpl schoolService;

    @Test
    void createSchool_uppercasesCodeAndSaves() {
        when(schoolRepository.existsByCode("GVS")).thenReturn(false);
        when(schoolRepository.save(any(School.class))).thenAnswer(i -> i.getArgument(0));

        SchoolResponse response = schoolService.createSchool(new CreateSchoolRequest(
                "Govt Village School", "gvs", null, null, null, null, null, null, null));

        assertThat(response.code()).isEqualTo("GVS");
        assertThat(response.name()).isEqualTo("Govt Village School");
    }

    @Test
    void createSchool_duplicateCode_throws() {
        when(schoolRepository.existsByCode("GVS")).thenReturn(true);

        assertThatThrownBy(() -> schoolService.createSchool(new CreateSchoolRequest(
                "X", "GVS", null, null, null, null, null, null, null)))
                .isInstanceOf(DuplicateResourceException.class);

        verify(schoolRepository, never()).save(any());
    }

    @Test
    void createSchoolAdmin_success_provisionsAuthUserAndSaves() {
        UUID schoolId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(School.builder().id(schoolId).build()));
        when(schoolAdminRepository.existsBySchoolId(schoolId)).thenReturn(false);
        when(authClient.createAdmin(any())).thenReturn(new AuthUserResponse(userId, "admin@x.test", "ADMIN", schoolId));
        when(schoolAdminRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        SchoolAdminResponse response = schoolService.createSchoolAdmin(schoolId,
                new CreateSchoolAdminRequest("Admin", "admin@x.test", "999", "Strong@123"));

        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.schoolId()).isEqualTo(schoolId);
    }

    @Test
    void createSchoolAdmin_whenAdminExists_throws() {
        UUID schoolId = UUID.randomUUID();
        when(schoolRepository.findById(schoolId)).thenReturn(Optional.of(School.builder().id(schoolId).build()));
        when(schoolAdminRepository.existsBySchoolId(schoolId)).thenReturn(true);

        assertThatThrownBy(() -> schoolService.createSchoolAdmin(schoolId,
                new CreateSchoolAdminRequest("Admin", "admin@x.test", "999", "Strong@123")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(authClient, never()).createAdmin(any());
    }
}
