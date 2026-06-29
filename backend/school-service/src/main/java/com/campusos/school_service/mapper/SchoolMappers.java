package com.campusos.school_service.mapper;

import com.campusos.common_lib.contract.RosterStudent;
import com.campusos.common_lib.contract.SchoolSummary;
import com.campusos.common_lib.contract.StudentSummary;
import com.campusos.school_service.dto.response.*;
import com.campusos.school_service.entity.ClassTeacher;
import com.campusos.school_service.entity.School;
import com.campusos.school_service.entity.SchoolAdmin;
import com.campusos.school_service.entity.Student;
import com.campusos.school_service.entity.Teacher;

public final class SchoolMappers {

    private SchoolMappers() {
    }

    public static SchoolResponse toSchoolResponse(School s) {
        return new SchoolResponse(s.getId(), s.getName(), s.getCode(), s.getAddress(), s.getVillage(),
                s.getDistrict(), s.getState(), s.getPincode(), s.getPhone(), s.getEmail(), s.getLogoUrl(), s.getActive());
    }

    public static SchoolSummary toSchoolSummary(School s) {
        return new SchoolSummary(s.getId(), s.getName(), s.getCode(), s.getLogoUrl());
    }

    public static SchoolAdminResponse toAdminResponse(SchoolAdmin a) {
        return new SchoolAdminResponse(a.getId(), a.getSchoolId(), a.getUserId(), a.getFullName(), a.getPhone());
    }

    public static ClassResponse toClassResponse(ClassTeacher c, String teacherName) {
        return new ClassResponse(c.getId(), c.getSchoolId(), c.getClassLabel(), c.getTeacherId(),
                teacherName, c.getAcademicYear());
    }

    public static TeacherResponse toTeacherResponse(Teacher t) {
        return new TeacherResponse(t.getId(), t.getSchoolId(), t.getUserId(), t.getFullName(), t.getEmail(),
                t.getPhone(), t.getQualification(), t.getSalary(), t.getActive());
    }

    public static TeacherContactDto toTeacherContact(Teacher t) {
        return new TeacherContactDto(t.getId(), t.getSchoolId(), t.getFullName(), t.getEmail(), t.getPhone());
    }

    public static StudentResponse toStudentResponse(Student s) {
        return new StudentResponse(s.getId(), s.getSchoolId(), s.getAdmissionNo(), s.getFullName(), s.getGender(),
                s.getDateOfBirth(), s.getClassLabel(), s.getFatherName(), s.getMotherName(), s.getGuardianPhone(),
                s.getAddress(), s.getVillage(), s.getPhotoUrl(), s.getHasBus(), s.getBusPickupPoint(),
                s.getActive(), s.getAdmissionDate());
    }

    public static RosterEntry toRosterEntry(Student s) {
        return new RosterEntry(s.getId(), s.getAdmissionNo(), s.getFullName(), s.getGuardianPhone(), s.getClassLabel());
    }

    public static StudentContactDto toStudentContact(Student s) {
        return new StudentContactDto(s.getId(), s.getSchoolId(), s.getAdmissionNo(), s.getFullName(),
                s.getClassLabel(), s.getGuardianPhone());
    }

    public static RosterStudent toRosterStudent(Student s) {
        return new RosterStudent(s.getId(), s.getSchoolId(), s.getAdmissionNo(), s.getFullName(),
                s.getClassLabel(), s.getGuardianPhone(), Boolean.TRUE.equals(s.getHasBus()));
    }

    public static StudentSummary toStudentSummary(Student s, String classTeacherName) {
        return new StudentSummary(s.getId(), s.getSchoolId(), s.getAdmissionNo(), s.getFullName(),
                s.getClassLabel(), sectionOf(s.getClassLabel()), classTeacherName);
    }

    /** "6-A" -> "A"; returns null when there is no section suffix. */
    public static String sectionOf(String classLabel) {
        if (classLabel == null) {
            return null;
        }
        int dash = classLabel.lastIndexOf('-');
        return dash >= 0 && dash < classLabel.length() - 1 ? classLabel.substring(dash + 1) : null;
    }
}
