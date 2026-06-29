package com.campusos.academic_service.client;

import com.campusos.common_lib.contract.RosterStudent;
import com.campusos.common_lib.contract.StudentSummary;
import com.campusos.common_lib.contract.TeacherClassView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

/**
 * Reads roster/teacher data from school-service's internal endpoints (no gateway,
 * no JWT). Used to scope teacher actions to their class and to enrich rosters.
 */
@FeignClient(name = "school-service", path = "/api/internal")
public interface SchoolClient {

    @GetMapping("/teachers/by-user/{userId}")
    TeacherClassView getTeacherClass(@PathVariable("userId") UUID userId);

    @GetMapping("/students/roster")
    List<RosterStudent> getRoster(@RequestParam("schoolId") UUID schoolId,
                                  @RequestParam("classLabel") String classLabel);

    @GetMapping("/students/lookup")
    StudentSummary lookupStudent(@RequestParam("schoolId") UUID schoolId,
                                 @RequestParam("admissionNo") String admissionNo);
}
