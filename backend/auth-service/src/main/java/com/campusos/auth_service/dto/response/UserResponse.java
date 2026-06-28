package com.campusos.auth_service.dto.response;

import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter // Generates getters
@Setter // Generates setters
@NoArgsConstructor // No-args constructor
@AllArgsConstructor // All-args constructor
@Builder // Builder Pattern
public class UserResponse {

    private UUID id;

    private String username;

    private String email;

    private Set<String> roles;
}
