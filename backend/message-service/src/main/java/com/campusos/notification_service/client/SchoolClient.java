package com.campusos.notification_service.client;

import com.campusos.common_lib.contract.RecipientContact;
import com.campusos.common_lib.contract.RosterStudent;
import com.campusos.notification_service.client.dto.BirthdayStudent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Resolves teacher recipients, class rosters, and birthday students from school-service. */
@FeignClient(name = "school-service", path = "/api/internal")
public interface SchoolClient {

    @GetMapping("/schools/{schoolId}/teacher-recipients")
    List<RecipientContact> teacherRecipients(@PathVariable("schoolId") UUID schoolId);

    @GetMapping("/students/roster")
    List<RosterStudent> getRoster(@RequestParam("schoolId") UUID schoolId,
                                  @RequestParam("classLabel") String classLabel);

    @GetMapping("/students/birthdays")
    List<BirthdayStudent> birthdays(@RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date);
}
