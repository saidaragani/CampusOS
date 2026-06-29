package com.campusos.school_service.controller;

import com.campusos.common_lib.contract.RecipientContact;
import com.campusos.common_lib.contract.RosterStudent;
import com.campusos.common_lib.contract.SchoolSummary;
import com.campusos.common_lib.contract.StudentSummary;
import com.campusos.common_lib.contract.TeacherClassView;
import com.campusos.school_service.dto.response.StudentContactDto;
import com.campusos.school_service.dto.response.TeacherContactDto;
import com.campusos.school_service.service.SchoolService;
import com.campusos.school_service.service.StudentService;
import com.campusos.school_service.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service-to-service endpoints. NOT routed by the gateway — reachable only from
 * inside the cluster (auth/academic/fee/messaging via Feign), so no user JWT.
 */
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
public class InternalController {

    private final StudentService studentService;
    private final SchoolService schoolService;
    private final TeacherService teacherService;

    /** Validate an admission number + resolve the student's class context (used by auth-service). */
    @GetMapping("/students/lookup")
    public ResponseEntity<StudentSummary> lookupStudent(@RequestParam UUID schoolId,
                                                        @RequestParam String admissionNo) {
        return ResponseEntity.ok(studentService.lookupByAdmission(schoolId, admissionNo));
    }

    /** Students of a class (used by academic/fee services). */
    @GetMapping("/students/by-class")
    public ResponseEntity<List<StudentContactDto>> studentsByClass(@RequestParam UUID classId) {
        return ResponseEntity.ok(studentService.byClass(classId));
    }

    /** Roster of a class by (schoolId, classLabel) — used by academic-service. */
    @GetMapping("/students/roster")
    public ResponseEntity<List<RosterStudent>> roster(@RequestParam UUID schoolId,
                                                      @RequestParam String classLabel) {
        return ResponseEntity.ok(studentService.roster(schoolId, classLabel));
    }

    /** Resolve a teacher's identity + assigned class from their auth user id. */
    @GetMapping("/teachers/by-user/{userId}")
    public ResponseEntity<TeacherClassView> teacherByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(teacherService.getClassViewByUser(userId));
    }

    /** Students with a birthday on the given date (used by messaging service). */
    @GetMapping("/students/birthdays")
    public ResponseEntity<List<StudentContactDto>> birthdays(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(studentService.birthdays(date));
    }

    /** School header (name + logo) — used by auth-service for the parent portal. */
    @GetMapping("/schools/{id}")
    public ResponseEntity<SchoolSummary> school(@PathVariable UUID id) {
        return ResponseEntity.ok(schoolService.getSchoolSummary(id));
    }

    /** Teacher contact for notifications (used by academic/messaging services). */
    @GetMapping("/teachers/{id}")
    public ResponseEntity<TeacherContactDto> teacher(@PathVariable UUID id) {
        return ResponseEntity.ok(teacherService.getContact(id));
    }

    /** Active teachers' email recipients for a school (messaging service). */
    @GetMapping("/schools/{schoolId}/teacher-recipients")
    public ResponseEntity<List<RecipientContact>> teacherRecipients(@PathVariable UUID schoolId) {
        return ResponseEntity.ok(teacherService.getTeacherRecipients(schoolId));
    }
}
