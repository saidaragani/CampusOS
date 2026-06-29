package com.campusos.auth_service.serviceimpl;

import com.campusos.auth_service.client.NotificationClient;
import com.campusos.auth_service.client.SchoolClient;
import com.campusos.auth_service.client.StudentClient;
import com.campusos.auth_service.dto.request.*;
import com.campusos.auth_service.dto.response.AuthResponse;
import com.campusos.auth_service.dto.response.ChildView;
import com.campusos.auth_service.dto.response.ParentContextResponse;
import com.campusos.auth_service.dto.response.UserSummaryDto;
import com.campusos.auth_service.entity.ParentStudentLink;
import com.campusos.auth_service.entity.PasswordResetToken;
import com.campusos.auth_service.entity.RefreshToken;
import com.campusos.auth_service.entity.Role;
import com.campusos.auth_service.entity.User;
import com.campusos.auth_service.enums.RoleType;
import com.campusos.auth_service.exception.BadRequestException;
import com.campusos.auth_service.exception.DuplicateResourceException;
import com.campusos.auth_service.exception.ResourceNotFoundException;
import com.campusos.auth_service.exception.ServiceUnavailableException;
import com.campusos.auth_service.repository.ParentStudentLinkRepository;
import com.campusos.auth_service.repository.PasswordResetTokenRepository;
import com.campusos.auth_service.repository.UserRepository;
import com.campusos.auth_service.security.User.UserPrincipal;
import com.campusos.auth_service.security.jwt.JwtService;
import com.campusos.common_lib.contract.SchoolSummary;
import com.campusos.common_lib.contract.StudentSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ParentStudentLinkRepository parentStudentLinkRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private com.campusos.auth_service.service.RoleService roleService;
    @Mock private com.campusos.auth_service.service.RefreshTokenService refreshTokenService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private StudentClient studentClient;
    @Mock private SchoolClient schoolClient;
    @Mock private NotificationClient notificationClient;

    @InjectMocks private AuthServiceImpl authService;

    private final UUID schoolId = UUID.randomUUID();
    private static final String ADMISSION = "GVS-2025-0001";

    private Role role(RoleType type) {
        return Role.builder().id(UUID.randomUUID()).name(type).build();
    }

    private User user(RoleType type) {
        return User.builder()
                .id(UUID.randomUUID())
                .email("user@campusos.com")
                .password("hashed")
                .fullName("Test User")
                .role(role(type))
                .schoolId(schoolId)
                .enabled(true)
                .build();
    }

    private StudentSummary studentSummary(UUID studentId) {
        return new StudentSummary(studentId, schoolId, ADMISSION, "Child Name", "6-A", "A", "Ramesh");
    }

    // ---------------- login ----------------

    @Test
    void login_returnsTokensAndUser() {
        User u = user(RoleType.ADMIN);
        UserPrincipal principal = new UserPrincipal(u);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(principal)).thenReturn("access-token");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(3_600_000L);
        when(refreshTokenService.create(u)).thenReturn(
                RefreshToken.builder().token("refresh-token").user(u).build());

        AuthResponse response = authService.login(new LoginRequest("user@campusos.com", "secret"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        assertThat(response.user().role()).isEqualTo("ADMIN");
        verify(userRepository).save(u); // lastLoginAt updated
    }

    // ---------------- registerParent ----------------

    private ParentRegistrationRequest parentRequest() {
        return new ParentRegistrationRequest("Parent One", "parent@campusos.com",
                "Strong@123", "9999999999", schoolId, ADMISSION);
    }

    @Test
    void registerParent_success_createsUserAndLink() {
        when(userRepository.existsByEmail("parent@campusos.com")).thenReturn(false);
        UUID studentId = UUID.randomUUID();
        when(studentClient.getByAdmission(schoolId, ADMISSION)).thenReturn(studentSummary(studentId));
        when(parentStudentLinkRepository.existsBySchoolIdAndAdmissionNo(schoolId, ADMISSION)).thenReturn(false);
        when(roleService.getByName(RoleType.PARENT)).thenReturn(role(RoleType.PARENT));
        when(passwordEncoder.encode("Strong@123")).thenReturn("hashed");

        authService.registerParent(parentRequest());

        verify(userRepository).save(any(User.class));
        verify(parentStudentLinkRepository).save(any());
    }

    @Test
    void registerParent_duplicateEmail_throws() {
        when(userRepository.existsByEmail("parent@campusos.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerParent(parentRequest()))
                .isInstanceOf(DuplicateResourceException.class);

        verifyNoInteractions(studentClient);
    }

    @Test
    void registerParent_admissionNotFound_throws() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        // student-service responds but with no student -> null studentId
        when(studentClient.getByAdmission(schoolId, ADMISSION))
                .thenReturn(new StudentSummary(null, schoolId, ADMISSION, null, null, null, null));

        assertThatThrownBy(() -> authService.registerParent(parentRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void registerParent_studentServiceDown_throwsServiceUnavailable() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(studentClient.getByAdmission(schoolId, ADMISSION))
                .thenThrow(new RuntimeException("connection refused"));

        assertThatThrownBy(() -> authService.registerParent(parentRequest()))
                .isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    void registerParent_childAlreadyClaimed_throws() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(studentClient.getByAdmission(schoolId, ADMISSION)).thenReturn(studentSummary(UUID.randomUUID()));
        when(parentStudentLinkRepository.existsBySchoolIdAndAdmissionNo(schoolId, ADMISSION)).thenReturn(true);

        assertThatThrownBy(() -> authService.registerParent(parentRequest()))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    // ---------------- createSchoolAdmin ----------------

    @Test
    void createSchoolAdmin_success() {
        CreateAdminRequest req = new CreateAdminRequest("Admin", "admin@campusos.com",
                "9999999999", "Strong@123", schoolId);
        when(userRepository.existsByEmail("admin@campusos.com")).thenReturn(false);
        when(roleService.getByName(RoleType.ADMIN)).thenReturn(role(RoleType.ADMIN));
        when(passwordEncoder.encode("Strong@123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserSummaryDto dto = authService.createSchoolAdmin(req);

        assertThat(dto.email()).isEqualTo("admin@campusos.com");
        assertThat(dto.role()).isEqualTo("ADMIN");
        assertThat(dto.schoolId()).isEqualTo(schoolId);
        verifyNoInteractions(schoolClient); // validate-school defaults to false
    }

    @Test
    void createSchoolAdmin_duplicateEmail_throws() {
        CreateAdminRequest req = new CreateAdminRequest("Admin", "admin@campusos.com",
                null, "Strong@123", schoolId);
        when(userRepository.existsByEmail("admin@campusos.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.createSchoolAdmin(req))
                .isInstanceOf(DuplicateResourceException.class);
    }

    // ---------------- createTeacher ----------------

    @Test
    void createTeacher_bindsToCallerSchool() {
        UUID teacherId = UUID.randomUUID();
        CreateTeacherRequest req = new CreateTeacherRequest("Ramesh", "ramesh@campusos.com",
                "9999999999", "Strong@123", teacherId);
        when(userRepository.existsByEmail("ramesh@campusos.com")).thenReturn(false);
        when(roleService.getByName(RoleType.TEACHER)).thenReturn(role(RoleType.TEACHER));
        when(passwordEncoder.encode("Strong@123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserSummaryDto dto = authService.createTeacher(req, schoolId);

        assertThat(dto.role()).isEqualTo("TEACHER");
        assertThat(dto.schoolId()).isEqualTo(schoolId);
    }

    @Test
    void createTeacher_nullCallerSchool_throwsBadRequest() {
        CreateTeacherRequest req = new CreateTeacherRequest("Ramesh", "ramesh@campusos.com",
                null, "Strong@123", UUID.randomUUID());

        assertThatThrownBy(() -> authService.createTeacher(req, null))
                .isInstanceOf(BadRequestException.class);
    }

    // ---------------- password flows ----------------

    @Test
    void changePassword_wrongOldPassword_throwsBadRequest() {
        User u = user(RoleType.PARENT);
        when(userRepository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(
                new ChangePasswordRequest("wrong", "Strong@123"), u.getEmail()))
                .isInstanceOf(BadRequestException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_success_updatesHash() {
        User u = user(RoleType.PARENT);
        when(userRepository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("old", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("Strong@123")).thenReturn("new-hash");

        authService.changePassword(new ChangePasswordRequest("old", "Strong@123"), u.getEmail());

        assertThat(u.getPassword()).isEqualTo("new-hash");
        verify(userRepository).save(u);
    }

    @Test
    void resetPassword_invalidToken_throws() {
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword(
                new ResetPasswordRequest("raw-token", "Strong@123")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void resetPassword_expiredToken_throws() {
        User u = user(RoleType.PARENT);
        PasswordResetToken token = PasswordResetToken.builder()
                .user(u)
                .tokenHash("hash")
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword(
                new ResetPasswordRequest("raw-token", "Strong@123")))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void resetPassword_success_updatesPasswordAndRevokesSessions() {
        User u = user(RoleType.PARENT);
        PasswordResetToken token = PasswordResetToken.builder()
                .user(u)
                .tokenHash("hash")
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        when(passwordResetTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("Strong@123")).thenReturn("new-hash");

        authService.resetPassword(new ResetPasswordRequest("raw-token", "Strong@123"));

        assertThat(u.getPassword()).isEqualTo("new-hash");
        assertThat(token.getUsedAt()).isNotNull();
        verify(refreshTokenService).revokeAllForUser(u);
    }

    @Test
    void forgotPassword_unknownEmail_isSilentNoOp() {
        when(userRepository.findByEmail("ghost@campusos.com")).thenReturn(Optional.empty());

        authService.forgotPassword(new ForgotPasswordRequest("ghost@campusos.com"));

        verify(passwordResetTokenRepository, never()).save(any());
        verifyNoInteractions(notificationClient);
    }

    @Test
    void forgotPassword_notificationFailure_isSwallowed() {
        User u = user(RoleType.PARENT);
        when(userRepository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        doThrow(new RuntimeException("message-service down"))
                .when(notificationClient).sendPasswordReset(any());

        // Must NOT throw — token is still persisted, email is best-effort.
        authService.forgotPassword(new ForgotPasswordRequest(u.getEmail()));

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    // ---------------- session ----------------

    @Test
    void refresh_delegatesToRefreshTokenService() {
        User u = user(RoleType.TEACHER);
        RefreshToken rotated = RefreshToken.builder().token("new-refresh").user(u).build();
        when(refreshTokenService.verifyAndRotate("old-refresh")).thenReturn(rotated);
        when(jwtService.generateToken(any())).thenReturn("new-access");
        when(jwtService.getAccessTokenExpirationMs()).thenReturn(3_600_000L);

        AuthResponse response = authService.refresh(new RefreshTokenRequest("old-refresh"));

        assertThat(response.accessToken()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
    }

    @Test
    void logout_revokesToken() {
        authService.logout(new RefreshTokenRequest("some-token"));
        verify(refreshTokenService).revoke("some-token");
    }

    // ---------------- children / parent context ----------------

    private ParentStudentLink link(User parent, UUID studentId) {
        return ParentStudentLink.builder()
                .studentId(studentId)
                .schoolId(schoolId)
                .admissionNo(ADMISSION)
                .parentUser(parent)
                .build();
    }

    @Test
    void getChildren_enrichesFromStudentService() {
        User u = user(RoleType.PARENT);
        UUID studentId = UUID.randomUUID();
        when(userRepository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        when(parentStudentLinkRepository.findAllByParentUser(u)).thenReturn(List.of(link(u, studentId)));
        when(studentClient.getByAdmission(schoolId, ADMISSION)).thenReturn(studentSummary(studentId));

        List<ChildView> children = authService.getChildren(u.getEmail());

        assertThat(children).hasSize(1);
        assertThat(children.get(0).admissionNo()).isEqualTo(ADMISSION);
        assertThat(children.get(0).classLabel()).isEqualTo("6-A");
        assertThat(children.get(0).classTeacherName()).isEqualTo("Ramesh");
    }

    @Test
    void getChildren_studentServiceDown_degradesGracefully() {
        User u = user(RoleType.PARENT);
        UUID studentId = UUID.randomUUID();
        when(userRepository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        when(parentStudentLinkRepository.findAllByParentUser(u)).thenReturn(List.of(link(u, studentId)));
        when(studentClient.getByAdmission(schoolId, ADMISSION)).thenThrow(new RuntimeException("down"));

        List<ChildView> children = authService.getChildren(u.getEmail());

        assertThat(children).hasSize(1);
        assertThat(children.get(0).admissionNo()).isEqualTo(ADMISSION);
        assertThat(children.get(0).studentId()).isEqualTo(studentId); // falls back to link data
        assertThat(children.get(0).classLabel()).isNull();
        assertThat(children.get(0).classTeacherName()).isNull();
    }

    @Test
    void getParentContext_includesSchoolHeaderAndChildren() {
        User u = user(RoleType.PARENT);
        UUID studentId = UUID.randomUUID();
        when(userRepository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        when(schoolClient.getSchool(schoolId))
                .thenReturn(new SchoolSummary(schoolId, "Govt Village School", "GVS", "https://cdn/logo.png"));
        when(parentStudentLinkRepository.findAllByParentUser(u)).thenReturn(List.of(link(u, studentId)));
        when(studentClient.getByAdmission(schoolId, ADMISSION)).thenReturn(studentSummary(studentId));

        ParentContextResponse context = authService.getParentContext(u.getEmail());

        assertThat(context.user().role()).isEqualTo("PARENT");
        assertThat(context.school().name()).isEqualTo("Govt Village School");
        assertThat(context.school().logoUrl()).isEqualTo("https://cdn/logo.png");
        assertThat(context.children()).hasSize(1);
        assertThat(context.children().get(0).classLabel()).isEqualTo("6-A");
    }

    @Test
    void getParentContext_schoolServiceDown_returnsNullSchoolButStillWorks() {
        User u = user(RoleType.PARENT);
        when(userRepository.findByEmail(u.getEmail())).thenReturn(Optional.of(u));
        when(schoolClient.getSchool(schoolId)).thenThrow(new RuntimeException("down"));
        when(parentStudentLinkRepository.findAllByParentUser(u)).thenReturn(List.of());

        ParentContextResponse context = authService.getParentContext(u.getEmail());

        assertThat(context.school()).isNull();
        assertThat(context.user().email()).isEqualTo(u.getEmail());
        assertThat(context.children()).isEmpty();
    }
}
