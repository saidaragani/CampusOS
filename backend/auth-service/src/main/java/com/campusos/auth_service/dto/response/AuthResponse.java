package com.campusos.auth_service.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;

    private UserResponse user;

    private String refreshToken;

    private String tokenType;

    private Long expiresAt;
}
