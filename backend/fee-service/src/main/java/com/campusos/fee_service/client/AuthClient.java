package com.campusos.fee_service.client;

import com.campusos.common_lib.contract.ChildLink;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

/** Reads a parent's children from auth-service to authorize fee access. */
@FeignClient(name = "auth-service", path = "/api/internal")
public interface AuthClient {

    @GetMapping("/parents/{userId}/children")
    List<ChildLink> getChildren(@PathVariable("userId") UUID userId);
}
