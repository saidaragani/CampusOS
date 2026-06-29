package com.campusos.auth_service.client;

import com.campusos.common_lib.contract.SchoolSummary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Seam to school-service. Supplies the school header (name + logo) for the parent
 * portal and validates a school exists when provisioning an admin. Calls the
 * internal (non-gateway) endpoint.
 */
@FeignClient(name = "school-service", contextId = "schoolClient", path = "/api/internal/schools")
public interface SchoolClient {

    @GetMapping("/{schoolId}")
    SchoolSummary getSchool(@PathVariable("schoolId") UUID schoolId);
}
