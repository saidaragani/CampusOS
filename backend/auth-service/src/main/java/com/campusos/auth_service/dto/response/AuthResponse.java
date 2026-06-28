package com.campusos.auth_service.dto.response;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn,
    UserSummaryDto user
) {}
