package com.campusos.school_service.serviceimpl;

import com.campusos.common_lib.contract.RosterStudent;
import com.campusos.common_lib.contract.StudentSummary;
import com.campusos.school_service.dto.request.CreateStudentRequest;
import com.campusos.school_service.dto.request.UpdateStudentRequest;
import com.campusos.school_service.dto.response.StudentContactDto;
import com.campusos.school_service.dto.response.StudentResponse;
import com.campusos.school_service.entity.ClassTeacher;
import com.campusos.school_service.entity.School;
import com.campusos.school_service.entity.Student;
import com.campusos.school_service.exception.ResourceNotFoundException;
import com.campusos.school_service.mapper.SchoolMappers;
import com.campusos.school_service.repository.ClassTeacherRepository;
import com.campusos.school_service.repository.SchoolRepository;
import com.campusos.school_service.repository.StudentRepository;
import com.campusos.school_service.repository.TeacherRepository;
import com.campusos.school_service.service.AdmissionNumberService;
import com.campusos.school_service.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final ClassTeacherRepository classTeacherRepository;
    private final TeacherRepository teacherRepository;
    private final AdmissionNumberService admissionNumberService;

    // ---------------- admin-facing ----------------

    @Override
    @Transactional
    public StudentResponse createStudent(UUID schoolId, CreateStudentRequest request) {
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School " + schoolId + " not found."));

        LocalDate admissionDate = request.admissionDate() != null ? request.admissionDate() : LocalDate.now();
        int year = admissionDate.getYear();
        String admissionNo = admissionNumberService.generate(schoolId, school.getCode(), year);

        Student student = Student.builder()
                .schoolId(schoolId)
                .admissionNo(admissionNo)
                .fullName(request.fullName())
                .gender(request.gender())
                .dateOfBirth(request.dateOfBirth())
                .classLabel(request.classLabel().trim())
                .fatherName(request.fatherName())
                .motherName(request.motherName())
                .guardianPhone(request.guardianPhone())
                .address(request.address())
                .village(request.village())
                .hasBus(request.hasBus() != null && request.hasBus())
                .busPickupPoint(request.busPickupPoint())
                .admissionDate(admissionDate)
                .active(true)
                .build();
        return SchoolMappers.toStudentResponse(studentRepository.save(student));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> listStudents(UUID schoolId, String classLabel, Pageable pageable) {
        Page<Student> page = (classLabel == null || classLabel.isBlank())
                ? studentRepository.findBySchoolId(schoolId, pageable)
                : studentRepository.findBySchoolIdAndClassLabel(schoolId, classLabel.trim(), pageable);
        return page.map(SchoolMappers::toStudentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudent(UUID schoolId, UUID id) {
        return SchoolMappers.toStudentResponse(requireStudent(schoolId, id));
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(UUID schoolId, UUID id, UpdateStudentRequest request) {
        Student s = requireStudent(schoolId, id);
        if (request.fullName() != null) s.setFullName(request.fullName());
        if (request.gender() != null) s.setGender(request.gender());
        if (request.dateOfBirth() != null) s.setDateOfBirth(request.dateOfBirth());
        if (request.classLabel() != null) s.setClassLabel(request.classLabel().trim());
        if (request.fatherName() != null) s.setFatherName(request.fatherName());
        if (request.motherName() != null) s.setMotherName(request.motherName());
        if (request.guardianPhone() != null) s.setGuardianPhone(request.guardianPhone());
        if (request.address() != null) s.setAddress(request.address());
        if (request.village() != null) s.setVillage(request.village());
        if (request.hasBus() != null) s.setHasBus(request.hasBus());
        if (request.busPickupPoint() != null) s.setBusPickupPoint(request.busPickupPoint());
        if (request.active() != null) s.setActive(request.active());
        return SchoolMappers.toStudentResponse(studentRepository.save(s));
    }

    @Override
    @Transactional
    public void deactivateStudent(UUID schoolId, UUID id) {
        Student s = requireStudent(schoolId, id);
        s.setActive(false);
        studentRepository.save(s);
    }

    @Override
    @Transactional
    public StudentResponse updatePhoto(UUID schoolId, UUID id, String photoUrl) {
        Student s = requireStudent(schoolId, id);
        s.setPhotoUrl(photoUrl);
        return SchoolMappers.toStudentResponse(studentRepository.save(s));
    }

    // ---------------- internal (service-to-service) ----------------

    @Override
    @Transactional(readOnly = true)
    public StudentSummary lookupByAdmission(UUID schoolId, String admissionNo) {
        Student s = studentRepository.findBySchoolIdAndAdmissionNo(schoolId, admissionNo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No student with admission number " + admissionNo + " in this school."));
        return SchoolMappers.toStudentSummary(s, classTeacherName(schoolId, s.getClassLabel()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RosterStudent> roster(UUID schoolId, String classLabel) {
        return studentRepository.findBySchoolIdAndClassLabelAndActiveTrue(schoolId, classLabel).stream()
                .map(SchoolMappers::toRosterStudent)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentContactDto> byClass(UUID classId) {
        ClassTeacher c = classTeacherRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class " + classId + " not found."));
        return studentRepository.findBySchoolIdAndClassLabelAndActiveTrue(c.getSchoolId(), c.getClassLabel()).stream()
                .map(SchoolMappers::toStudentContact)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentContactDto> birthdays(LocalDate date) {
        return studentRepository.findByBirthday(date.getMonthValue(), date.getDayOfMonth()).stream()
                .map(SchoolMappers::toStudentContact)
                .toList();
    }

    private Student requireStudent(UUID schoolId, UUID id) {
        return studentRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student " + id + " not found in this school."));
    }

    private String classTeacherName(UUID schoolId, String classLabel) {
        return classTeacherRepository.findBySchoolIdAndClassLabel(schoolId, classLabel)
                .flatMap(ct -> teacherRepository.findById(ct.getTeacherId()))
                .map(t -> t.getFullName())
                .orElse(null);
    }
}
