package com.campusos.academic_service.serviceimpl;

import com.campusos.academic_service.dto.request.PostClassUpdateRequest;
import com.campusos.academic_service.dto.response.ClassUpdateResponse;
import com.campusos.academic_service.entity.ClassUpdate;
import com.campusos.academic_service.enums.ClassUpdateType;
import com.campusos.academic_service.exception.ForbiddenException;
import com.campusos.academic_service.exception.ResourceNotFoundException;
import com.campusos.academic_service.mapper.AcademicMappers;
import com.campusos.academic_service.repository.ClassUpdateRepository;
import com.campusos.academic_service.service.ClassUpdateService;
import com.campusos.academic_service.support.AcademicAccessResolver;
import com.campusos.common_lib.contract.TeacherClassView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassUpdateServiceImpl implements ClassUpdateService {

    private final ClassUpdateRepository classUpdateRepository;
    private final AcademicAccessResolver access;

    @Override
    @Transactional
    public ClassUpdateResponse postUpdate(UUID teacherUserId, PostClassUpdateRequest request) {
        TeacherClassView tc = access.requireTeacherClass(teacherUserId);
        ClassUpdate update = ClassUpdate.builder()
                .schoolId(tc.schoolId())
                .classLabel(tc.classLabel())
                .title(request.title())
                .body(request.body())
                .type(request.type() != null ? request.type() : ClassUpdateType.NOTE)
                .postedByTeacherId(tc.teacherId())
                .build();
        return AcademicMappers.toClassUpdateResponse(classUpdateRepository.save(update));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassUpdateResponse> getClassFeed(UUID schoolId, String classLabel) {
        return classUpdateRepository.findBySchoolIdAndClassLabelOrderByCreatedAtDesc(schoolId, classLabel).stream()
                .map(AcademicMappers::toClassUpdateResponse).toList();
    }

    @Override
    @Transactional
    public void deleteUpdate(UUID teacherUserId, UUID id) {
        TeacherClassView tc = access.requireTeacherClass(teacherUserId);
        ClassUpdate update = classUpdateRepository.findByIdAndSchoolId(id, tc.schoolId())
                .orElseThrow(() -> new ResourceNotFoundException("Class update not found."));
        if (!update.getClassLabel().equals(tc.classLabel())) {
            throw new ForbiddenException("That update is not for your class.");
        }
        classUpdateRepository.delete(update);
    }
}
