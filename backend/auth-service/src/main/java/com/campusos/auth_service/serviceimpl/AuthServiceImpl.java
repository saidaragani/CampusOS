package com.campusos.auth_service.serviceimpl;

import com.campusos.auth_service.dto.request.LoginRequest;
import com.campusos.auth_service.dto.request.RefreshTokenRequest;
import com.campusos.auth_service.dto.request.RegisterRequest;
import com.campusos.auth_service.dto.response.AuthResponse;
import com.campusos.auth_service.repository.RefreshTokenRepository;
import com.campusos.auth_service.repository.RoleRepository;
import com.campusos.auth_service.repository.UserRepository;
import com.campusos.auth_service.security.jwt.JwtService;
import com.campusos.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor 
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        return null;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        return null;
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        return null;
    }

    @Override
    public void logout(String email) {

    }
}
