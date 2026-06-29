package com.campusos.auth_service.client;

import com.campusos.common_lib.contract.StudentSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/**
 * Seam to school-service (which owns students). Used during parent
 * self-registration / child-linking to confirm an admission number belongs to a
 * real student and to resolve the student's id + class context. Calls the
 * internal (non-gateway) endpoint; failures translate to 503 in the service layer.
 */
@FeignClient(name = "school-service", contextId = "studentClient", path = "/api/internal/students")
public interface StudentClient {

    @GetMapping("/lookup")
    StudentSummary getByAdmission(
            @RequestParam("schoolId") UUID schoolId,
            @RequestParam("admissionNo") String admissionNo
    );
}
