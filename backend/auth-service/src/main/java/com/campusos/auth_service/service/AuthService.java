package com.campusos.auth_service.service;

import com.campusos.auth_service.dto.request.*;
import com.campusos.auth_service.dto.response.AuthResponse;
import com.campusos.auth_service.dto.response.ChildView;
import com.campusos.auth_service.dto.response.ParentContextResponse;
import com.campusos.auth_service.dto.response.UserSummaryDto;
import com.campusos.common_lib.contract.ChildLink;
import com.campusos.common_lib.contract.RecipientContact;

import java.util.List;
import java.util.UUID;

public interface AuthService {

    // --- Authentication / session ---
    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    // --- Account provisioning (the arc) ---
    void registerParent(ParentRegistrationRequest request);

    UserSummaryDto createSchoolAdmin(CreateAdminRequest request);

    UserSummaryDto createTeacher(CreateTeacherRequest request, UUID callerSchoolId);

    // --- Password lifecycle ---
    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request, String userEmail);

    // --- Self-service ---
    UserSummaryDto getCurrentUser(String userEmail);

    void linkChild(LinkChildRequest request, String userEmail);

    List<ChildView> getChildren(String userEmail);

    /** Aggregated view for the parent portal: user + school header + children. */
    ParentContextResponse getParentContext(String userEmail);

    /** Internal: a parent's claimed children, by auth user id (for cross-service authorization). */
    List<ChildLink> getChildLinksByUserId(UUID userId);

    /** Internal: parent email recipients for a student (for the messaging service). */
    List<RecipientContact> getStudentParentRecipients(UUID studentId);

    /** Internal: all distinct parent email recipients in a school (for the messaging service). */
    List<RecipientContact> getSchoolParentRecipients(UUID schoolId);
}
