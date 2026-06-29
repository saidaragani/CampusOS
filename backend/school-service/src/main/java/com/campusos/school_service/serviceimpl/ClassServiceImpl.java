package com.campusos.school_service.serviceimpl;

import com.campusos.school_service.dto.request.CreateClassRequest;
import com.campusos.school_service.dto.response.ClassResponse;
import com.campusos.school_service.entity.ClassTeacher;
import com.campusos.school_service.entity.Teacher;
import com.campusos.school_service.exception.BadRequestException;
import com.campusos.school_service.exception.DuplicateResourceException;
import com.campusos.school_service.exception.ResourceNotFoundException;
import com.campusos.school_service.mapper.SchoolMappers;
import com.campusos.school_service.repository.ClassTeacherRepository;
import com.campusos.school_service.repository.StudentRepository;
import com.campusos.school_service.repository.TeacherRepository;
import com.campusos.school_service.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassTeacherRepository classTeacherRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public ClassResponse createClass(UUID schoolId, CreateClassRequest request) {
        String classLabel = request.classLabel().trim();
        if (classTeacherRepository.existsBySchoolIdAndClassLabel(schoolId, classLabel)) {
            throw new DuplicateResourceException("Class " + classLabel + " already exists in this school.");
        }
        Teacher teacher = requireTeacher(schoolId, request.teacherId());

        ClassTeacher entity = ClassTeacher.builder()
                .schoolId(schoolId)
                .classLabel(classLabel)
                .teacherId(teacher.getId())
                .academicYear(request.academicYear())
                .build();
        return SchoolMappers.toClassResponse(classTeacherRepository.save(entity), teacher.getFullName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassResponse> listClasses(UUID schoolId) {
        return classTeacherRepository.findBySchoolId(schoolId).stream()
                .map(c -> SchoolMappers.toClassResponse(c, teacherName(c.getTeacherId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassResponse getClass(UUID schoolId, UUID id) {
        ClassTeacher c = requireClass(schoolId, id);
        return SchoolMappers.toClassResponse(c, teacherName(c.getTeacherId()));
    }

    @Override
    @Transactional
    public ClassResponse updateClass(UUID schoolId, UUID id, String academicYear) {
        ClassTeacher c = requireClass(schoolId, id);
        if (academicYear != null) {
            c.setAcademicYear(academicYear);
        }
        return SchoolMappers.toClassResponse(classTeacherRepository.save(c), teacherName(c.getTeacherId()));
    }

    @Override
    @Transactional
    public ClassResponse assignTeacher(UUID schoolId, UUID id, UUID teacherId) {
        ClassTeacher c = requireClass(schoolId, id);
        Teacher teacher = requireTeacher(schoolId, teacherId);
        c.setTeacherId(teacher.getId());
        return SchoolMappers.toClassResponse(classTeacherRepository.save(c), teacher.getFullName());
    }

    @Override
    @Transactional
    public void deleteClass(UUID schoolId, UUID id) {
        ClassTeacher c = requireClass(schoolId, id);
        boolean hasStudents = !studentRepository
                .findBySchoolIdAndClassLabelAndActiveTrue(schoolId, c.getClassLabel()).isEmpty();
        if (hasStudents) {
            throw new BadRequestException("Cannot delete a class that still has students.");
        }
        classTeacherRepository.delete(c);
    }

    private ClassTeacher requireClass(UUID schoolId, UUID id) {
        return classTeacherRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Class " + id + " not found in this school."));
    }

    private Teacher requireTeacher(UUID schoolId, UUID teacherId) {
        return teacherRepository.findByIdAndSchoolId(teacherId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher " + teacherId + " not found in this school."));
    }

    private String teacherName(UUID teacherId) {
        return teacherRepository.findById(teacherId).map(Teacher::getFullName).orElse(null);
    }
}
