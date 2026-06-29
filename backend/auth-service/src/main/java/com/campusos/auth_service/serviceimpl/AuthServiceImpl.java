package com.campusos.auth_service.serviceimpl;

import com.campusos.auth_service.client.NotificationClient;
import com.campusos.auth_service.client.SchoolClient;
import com.campusos.auth_service.client.StudentClient;
import com.campusos.auth_service.dto.request.*;
import com.campusos.auth_service.dto.response.AuthResponse;
import com.campusos.auth_service.dto.response.ChildView;
import com.campusos.auth_service.dto.response.ParentContextResponse;
import com.campusos.auth_service.dto.response.UserSummaryDto;
import com.campusos.common_lib.contract.ChildLink;
import com.campusos.common_lib.contract.RecipientContact;
import com.campusos.common_lib.contract.PasswordResetNotification;
import com.campusos.common_lib.contract.SchoolSummary;
import com.campusos.common_lib.contract.StudentSummary;
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
import com.campusos.auth_service.mapper.UserMapper;
import com.campusos.auth_service.repository.ParentStudentLinkRepository;
import com.campusos.auth_service.repository.PasswordResetTokenRepository;
import com.campusos.auth_service.repository.UserRepository;
import com.campusos.auth_service.security.User.UserPrincipal;
import com.campusos.auth_service.security.jwt.JwtService;
import com.campusos.auth_service.service.AuthService;
import com.campusos.auth_service.service.RefreshTokenService;
import com.campusos.auth_service.service.RoleService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final long PASSWORD_RESET_TTL_HOURS = 1;

    private final UserRepository userRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final RoleService roleService;
    private final RefreshTokenService refreshTokenService;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final StudentClient studentClient;
    private final SchoolClient schoolClient;
    private final NotificationClient notificationClient;

    @Value("${app.validate-school:false}")
    private boolean validateSchool;

    private final SecureRandom secureRandom = new SecureRandom();

    // ------------------------------------------------------------------
    // Authentication / session
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return buildAuthResponse(user, principal);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken rotated = refreshTokenService.verifyAndRotate(request.refreshToken());
        User user = rotated.getUser();

        String accessToken = jwtService.generateToken(new UserPrincipal(user));
        return new AuthResponse(
                accessToken,
                rotated.getToken(),
                "Bearer",
                jwtService.getAccessTokenExpirationMs() / 1000,
                UserMapper.toUserSummaryDto(user)
        );
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    // ------------------------------------------------------------------
    // Account provisioning
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public void registerParent(ParentRegistrationRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use");
        }

        // Prove the admission number is real before creating anything.
        StudentSummary student = validateAdmission(request.schoolId(), request.admissionNo());

        // First parent to register wins the child (uniqueness on school + admission).
        if (parentStudentLinkRepository.existsBySchoolIdAndAdmissionNo(request.schoolId(), request.admissionNo())) {
            throw new DuplicateResourceException("This child has already been linked to a parent account.");
        }

        Role parentRole = roleService.getByName(RoleType.PARENT);

        User parent = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .phone(request.phone())
                .role(parentRole)
                .schoolId(request.schoolId())
                .enabled(true)
                .build();
        userRepository.save(parent);

        ParentStudentLink link = ParentStudentLink.builder()
                .parentUser(parent)
                .schoolId(request.schoolId())
                .studentId(student.studentId())
                .admissionNo(request.admissionNo())
                .build();
        parentStudentLinkRepository.save(link);
    }

    @Override
    @Transactional
    public UserSummaryDto createSchoolAdmin(CreateAdminRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use");
        }

        if (validateSchool) {
            verifySchoolExists(request.schoolId());
        }

        Role adminRole = roleService.getByName(RoleType.ADMIN);

        User admin = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .phone(request.phone())
                .role(adminRole)
                .schoolId(request.schoolId())
                .enabled(true)
                .build();

        return UserMapper.toUserSummaryDto(userRepository.save(admin));
    }

    @Override
    @Transactional
    public UserSummaryDto createTeacher(CreateTeacherRequest request, UUID callerSchoolId) {
        if (callerSchoolId == null) {
            throw new BadRequestException("Authenticated admin is not bound to a school.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use");
        }

        Role teacherRole = roleService.getByName(RoleType.TEACHER);

        User teacher = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .phone(request.phone())
                .role(teacherRole)
                .schoolId(callerSchoolId)
                .teacherId(request.teacherId())
                .enabled(true)
                .build();

        return UserMapper.toUserSummaryDto(userRepository.save(teacher));
    }

    // ------------------------------------------------------------------
    // Password lifecycle
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        // Always behaves the same whether or not the email exists (no enumeration).
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String rawToken = generateRawToken();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(sha256(rawToken))
                    .expiresAt(LocalDateTime.now().plusHours(PASSWORD_RESET_TTL_HOURS))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            // Best-effort: a failure here must not break the flow.
            try {
                notificationClient.sendPasswordReset(
                        new PasswordResetNotification(user.getEmail(), user.getFullName(), rawToken));
            } catch (Exception ex) {
                log.warn("Failed to dispatch password-reset email for {}: {}", user.getEmail(), ex.getMessage());
            }
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(sha256(request.token()))
                .orElseThrow(() -> new BadRequestException("Invalid password reset token"));

        if (!resetToken.isValid()) {
            throw new BadRequestException("Password reset token has expired or already been used.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        // Invalidate any active sessions after a reset.
        refreshTokenService.revokeAllForUser(user);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    // ------------------------------------------------------------------
    // Self-service
    // ------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDto getCurrentUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserMapper.toUserSummaryDto(user);
    }

    @Override
    @Transactional
    public void linkChild(LinkChildRequest request, String userEmail) {
        User parent = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        StudentSummary student = validateAdmission(request.schoolId(), request.admissionNo());

        if (parentStudentLinkRepository.existsBySchoolIdAndAdmissionNo(request.schoolId(), request.admissionNo())) {
            throw new DuplicateResourceException("This child has already been linked to a parent account.");
        }

        ParentStudentLink link = ParentStudentLink.builder()
                .parentUser(parent)
                .schoolId(request.schoolId())
                .studentId(student.studentId())
                .admissionNo(request.admissionNo())
                .build();
        parentStudentLinkRepository.save(link);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChildView> getChildren(String userEmail) {
        User parent = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return loadChildViews(parent);
    }

    @Override
    @Transactional(readOnly = true)
    public ParentContextResponse getParentContext(String userEmail) {
        User parent = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SchoolSummary school = safeGetSchool(parent.getSchoolId());
        return new ParentContextResponse(
                UserMapper.toUserSummaryDto(parent),
                school,
                loadChildViews(parent)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChildLink> getChildLinksByUserId(UUID userId) {
        User parent = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return parentStudentLinkRepository.findAllByParentUser(parent).stream()
                .map(link -> new ChildLink(link.getStudentId(), link.getSchoolId(), link.getAdmissionNo()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipientContact> getStudentParentRecipients(UUID studentId) {
        return parentStudentLinkRepository.findByStudentId(studentId).stream()
                .map(link -> toRecipient(link.getParentUser()))
                .filter(distinctByEmail())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipientContact> getSchoolParentRecipients(UUID schoolId) {
        return parentStudentLinkRepository.findBySchoolId(schoolId).stream()
                .map(link -> toRecipient(link.getParentUser()))
                .filter(distinctByEmail())
                .toList();
    }

    private RecipientContact toRecipient(User user) {
        return new RecipientContact(user.getEmail(), user.getFullName());
    }

    private java.util.function.Predicate<RecipientContact> distinctByEmail() {
        java.util.Set<String> seen = java.util.concurrent.ConcurrentHashMap.newKeySet();
        return r -> seen.add(r.email());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private AuthResponse buildAuthResponse(User user, UserPrincipal principal) {
        String accessToken = jwtService.generateToken(principal);
        RefreshToken refreshToken = refreshTokenService.create(user);
        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer",
                jwtService.getAccessTokenExpirationMs() / 1000,
                UserMapper.toUserSummaryDto(user)
        );
    }

    /**
     * Calls student-service to validate the admission number. Translates a 404
     * into "not found" and any transport failure into 503 — registration is
     * rejected, never silently accepted, while student-service is unavailable.
     */
    private StudentSummary validateAdmission(UUID schoolId, String admissionNo) {
        StudentSummary student;
        try {
            student = studentClient.getByAdmission(schoolId, admissionNo);
        } catch (FeignException.NotFound ex) {
            // student-service explicitly says the admission number doesn't exist.
            throw new ResourceNotFoundException(
                    "No student found for admission number " + admissionNo + " in this school.");
        } catch (Exception ex) {
            // Any other transport/server failure -> reject (never silently accept).
            log.warn("student-service unavailable while validating admission {}: {}", admissionNo, ex.getMessage());
            throw new ServiceUnavailableException(
                    "Student service is unavailable. Please try again later.");
        }

        if (student == null || student.studentId() == null) {
            throw new ResourceNotFoundException(
                    "No student found for admission number " + admissionNo + " in this school.");
        }
        return student;
    }

    private void verifySchoolExists(UUID schoolId) {
        try {
            if (schoolClient.getSchool(schoolId) == null) {
                throw new ResourceNotFoundException("School " + schoolId + " does not exist.");
            }
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("School " + schoolId + " does not exist.");
        } catch (FeignException ex) {
            throw new ServiceUnavailableException("School service is unavailable. Please try again later.");
        }
    }

    /**
     * Builds the parent-facing child views, enriching each with class/section/
     * teacher from student-service. Read enrichment is best-effort: if the
     * student-service is unavailable the child is still returned with its
     * admission number and null class context (the portal degrades gracefully
     * rather than failing).
     */
    private List<ChildView> loadChildViews(User parent) {
        return parentStudentLinkRepository.findAllByParentUser(parent).stream()
                .map(this::toChildView)
                .toList();
    }

    private ChildView toChildView(ParentStudentLink link) {
        StudentSummary student = safeGetStudent(link.getSchoolId(), link.getAdmissionNo());
        if (student != null) {
            return new ChildView(
                    student.studentId(),
                    link.getAdmissionNo(),
                    student.studentName(),
                    student.classLabel(),
                    student.section(),
                    student.classTeacherName()
            );
        }
        // Degrade to what auth-service itself knows.
        return new ChildView(link.getStudentId(), link.getAdmissionNo(), null, null, null, null);
    }

    private StudentSummary safeGetStudent(UUID schoolId, String admissionNo) {
        try {
            return studentClient.getByAdmission(schoolId, admissionNo);
        } catch (Exception ex) {
            log.warn("student-service unavailable while enriching admission {}: {}", admissionNo, ex.getMessage());
            return null;
        }
    }

    private SchoolSummary safeGetSchool(UUID schoolId) {
        if (schoolId == null) {
            return null;
        }
        try {
            return schoolClient.getSchool(schoolId);
        } catch (Exception ex) {
            log.warn("school-service unavailable while loading school {}: {}", schoolId, ex.getMessage());
            return null;
        }
    }

    private String generateRawToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm not available", ex);
        }
    }
}
