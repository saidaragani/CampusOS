package com.campusos.school_service.client;

import com.campusos.common_lib.contract.SchoolFeeStats;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/** Reads school-wide fee stats from fee-service (cross-school reports). */
@FeignClient(name = "fee-service", path = "/api/internal/fees")
public interface FeeStatsClient {

    @GetMapping("/school-stats")
    SchoolFeeStats getSchoolStats(@RequestParam("schoolId") UUID schoolId);
}
