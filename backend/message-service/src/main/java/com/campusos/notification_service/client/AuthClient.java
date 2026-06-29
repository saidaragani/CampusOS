package com.campusos.notification_service.client;

import com.campusos.common_lib.contract.RecipientContact;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

/** Resolves parent email recipients from auth-service. */
@FeignClient(name = "auth-service", path = "/api/internal")
public interface AuthClient {

    @GetMapping("/students/{studentId}/parent-recipients")
    List<RecipientContact> studentParentRecipients(@PathVariable("studentId") UUID studentId);

    @GetMapping("/schools/{schoolId}/parent-recipients")
    List<RecipientContact> schoolParentRecipients(@PathVariable("schoolId") UUID schoolId);
}
