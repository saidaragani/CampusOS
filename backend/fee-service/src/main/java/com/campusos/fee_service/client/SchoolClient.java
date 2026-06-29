package com.campusos.fee_service.client;

import com.campusos.common_lib.contract.RosterStudent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

/** Reads a class roster from school-service to generate per-student fees. */
@FeignClient(name = "school-service", path = "/api/internal")
public interface SchoolClient {

    @GetMapping("/students/roster")
    List<RosterStudent> getRoster(@RequestParam("schoolId") UUID schoolId,
                                  @RequestParam("classLabel") String classLabel);
}
