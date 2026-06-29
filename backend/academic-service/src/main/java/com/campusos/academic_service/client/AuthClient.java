package com.campusos.academic_service.client;

import com.campusos.common_lib.contract.ChildLink;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

/**
 * Reads a parent's claimed children from auth-service's internal endpoint, to
 * authorize parent access to a student.
 */
@FeignClient(name = "auth-service", path = "/api/internal")
public interface AuthClient {

    @GetMapping("/parents/{userId}/children")
    List<ChildLink> getChildren(@PathVariable("userId") UUID userId);
}
