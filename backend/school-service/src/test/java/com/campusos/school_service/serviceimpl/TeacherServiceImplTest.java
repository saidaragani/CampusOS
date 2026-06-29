package com.campusos.school_service.serviceimpl;

import com.campusos.school_service.client.AuthClient;
import com.campusos.school_service.client.dto.AuthUserResponse;
import com.campusos.school_service.dto.request.CreateTeacherRequest;
import com.campusos.school_service.dto.response.TeacherResponse;
import com.campusos.school_service.entity.Teacher;
import com.campusos.school_service.repository.ClassTeacherRepository;
import com.campusos.school_service.repository.StudentRepository;
import com.campusos.school_service.repository.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeacherServiceImplTest {

    @Mock private TeacherRepository teacherRepository;
    @Mock private ClassTeacherRepository classTeacherRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private AuthClient authClient;

    @InjectMocks private TeacherServiceImpl teacherService;

    @Test
    void createTeacher_savesRecordThenProvisionsLoginAndLinksUserId() {
        UUID schoolId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(teacherRepository.saveAndFlush(any(Teacher.class)))
                .thenAnswer(i -> {
                    Teacher t = i.getArgument(0);
                    t.setId(teacherId);
                    return t;
                });
        when(authClient.createTeacher(any()))
                .thenReturn(new AuthUserResponse(userId, "ramesh@x.test", "TEACHER", schoolId));
        when(teacherRepository.save(any(Teacher.class))).thenAnswer(i -> i.getArgument(0));

        TeacherResponse response = teacherService.createTeacher(schoolId, new CreateTeacherRequest(
                "Ramesh", "ramesh@x.test", "999", "B.Ed", new BigDecimal("35000"), "Strong@123"));

        assertThat(response.id()).isEqualTo(teacherId);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.schoolId()).isEqualTo(schoolId);
        assertThat(response.salary()).isEqualByComparingTo("35000");
    }
}
