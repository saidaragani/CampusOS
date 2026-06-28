package com.campusos.auth_service.service;

import com.campusos.auth_service.dto.request.LoginRequest;
import com.campusos.auth_service.dto.request.RefreshTokenRequest;
import com.campusos.auth_service.dto.request.RegisterRequest;
import com.campusos.auth_service.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String email);
}
