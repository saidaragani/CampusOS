package com.campusos.school_service.client;

import com.campusos.school_service.client.dto.AuthAdminRequest;
import com.campusos.school_service.client.dto.AuthTeacherRequest;
import com.campusos.school_service.client.dto.AuthUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Calls auth-service to provision login accounts when the admin/teacher domain
 * records are created here. The caller's JWT is forwarded (see
 * {@link FeignAuthForwardingConfig}) so auth enforces the right role.
 */
@FeignClient(name = "auth-service", path = "/api/auth", configuration = FeignAuthForwardingConfig.class)
public interface AuthClient {

    @PostMapping("/admins")
    AuthUserResponse createAdmin(@RequestBody AuthAdminRequest request);

    @PostMapping("/teachers")
    AuthUserResponse createTeacher(@RequestBody AuthTeacherRequest request);
}
