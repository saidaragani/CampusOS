package com.campusos.auth_service.service;

import com.campusos.auth_service.dto.request.*;
import com.campusos.auth_service.dto.response.AuthResponse;
import com.campusos.auth_service.dto.response.UserSummaryDto;

import java.util.List;

public interface AuthService {
    void signUpParent(ParentSignUpRequest request);
    AuthResponse login(LoginRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    void changePassword(ChangePasswordRequest request, String userEmail);
    UserSummaryDto getCurrentUser(String userEmail);
    
    void linkChild(LinkChildRequest request, String userEmail);
    List<ChildDto> getChildren(String userEmail);
}