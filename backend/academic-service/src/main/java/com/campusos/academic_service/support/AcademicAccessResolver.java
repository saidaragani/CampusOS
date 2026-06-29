package com.campusos.academic_service.support;

import com.campusos.academic_service.client.AuthClient;
import com.campusos.academic_service.client.SchoolClient;
import com.campusos.academic_service.exception.BadRequestException;
import com.campusos.academic_service.exception.ForbiddenException;
import com.campusos.academic_service.exception.ResourceNotFoundException;
import com.campusos.academic_service.exception.ServiceUnavailableException;
import com.campusos.common_lib.contract.ChildLink;
import com.campusos.common_lib.contract.RosterStudent;
import com.campusos.common_lib.contract.StudentSummary;
import com.campusos.common_lib.contract.TeacherClassView;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves cross-service context for academic operations: the calling teacher's
 * class (from school-service) and a parent's ownership of a student (from
 * auth-service). Translates downstream failures into clean 403/404/503.
 */
@Component
@RequiredArgsConstructor
public class AcademicAccessResolver {

    private final SchoolClient schoolClient;
    private final AuthClient authClient;

    /** Teacher identity + assigned class; fails if the teacher has no class yet. */
    public TeacherClassView requireTeacherClass(UUID userId) {
        TeacherClassView view = teacherInfo(userId);
        if (view.classLabel() == null) {
            throw new BadRequestException("You are not assigned to a class yet.");
        }
        return view;
    }

    /** Teacher identity (classLabel may be null). */
    public TeacherClassView teacherInfo(UUID userId) {
        try {
            return schoolClient.getTeacherClass(userId);
        } catch (FeignException.NotFound ex) {
            throw new ForbiddenException("No teacher record for the current user.");
        } catch (Exception ex) {
            throw new ServiceUnavailableException("School service is unavailable. Please try again later.");
        }
    }

    /** The parent's link to a student, or 403 if not their child. */
    public ChildLink requireOwnedChild(UUID parentUserId, UUID studentId) {
        List<ChildLink> children;
        try {
            children = authClient.getChildren(parentUserId);
        } catch (Exception ex) {
            throw new ServiceUnavailableException("Auth service is unavailable. Please try again later.");
        }
        return children.stream()
                .filter(c -> c.studentId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new ForbiddenException("This student is not linked to your account."));
    }

    public StudentSummary lookupStudent(UUID schoolId, String admissionNo) {
        try {
            return schoolClient.lookupStudent(schoolId, admissionNo);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Student not found.");
        } catch (Exception ex) {
            throw new ServiceUnavailableException("School service is unavailable. Please try again later.");
        }
    }

    public List<RosterStudent> roster(UUID schoolId, String classLabel) {
        try {
            return schoolClient.getRoster(schoolId, classLabel);
        } catch (Exception ex) {
            throw new ServiceUnavailableException("School service is unavailable. Please try again later.");
        }
    }
}
