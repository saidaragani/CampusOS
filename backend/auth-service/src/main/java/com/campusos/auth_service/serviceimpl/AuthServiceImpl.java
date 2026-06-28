package com.campusos.auth_service.serviceimpl;

import com.campusos.auth_service.dto.request.*;
import com.campusos.auth_service.dto.response.AuthResponse;
import com.campusos.auth_service.dto.response.UserSummaryDto;
import com.campusos.auth_service.entity.ParentStudentLink;
import com.campusos.auth_service.entity.PasswordResetToken;
import com.campusos.auth_service.entity.Role;
import com.campusos.auth_service.entity.User;
import com.campusos.auth_service.exception.DuplicateResourceException;
import com.campusos.auth_service.exception.ResourceNotFoundException;
import com.campusos.auth_service.mapper.ParentStudentLinkMapper;
import com.campusos.auth_service.mapper.UserMapper;
import com.campusos.auth_service.repository.ParentStudentLinkRepository;
import com.campusos.auth_service.repository.PasswordResetTokenRepository;
import com.campusos.auth_service.repository.UserRepository;
import com.campusos.auth_service.security.User.UserPrincipal;
import com.campusos.auth_service.security.jwt.JwtService;
import com.campusos.auth_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ParentStudentLinkRepository parentStudentLinkRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void signUpParent(ParentSignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use");
        }

        User parent = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(Role.PARENT)
                .enabled(true)
                .build();

        userRepository.save(parent);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userPrincipal.getUser();

        String accessToken = jwtService.generateToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);

        UserSummaryDto userSummary = UserMapper.INSTANCE.toUserSummaryDto(user);

        return new AuthResponse(accessToken, refreshToken, "Bearer", 3600L, userSummary);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .tokenHash(token) // Note: Keeping plain UUID here for straightforward lookup 
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();
            
            passwordResetTokenRepository.save(resetToken);
            
            // TODO: Integrate Email Service and send the link containing the token to the user
        });
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(request.token())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now()) || resetToken.getUsedAt() != null) {
            throw new DuplicateResourceException("Token expired or already used");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new ResourceNotFoundException("Invalid current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Override
    public UserSummaryDto getCurrentUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return UserMapper.INSTANCE.toUserSummaryDto(user);
    }

    @Override
    @Transactional
    public void linkChild(LinkChildRequest request, String userEmail) {
        User parent = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // TODO: Communicate with Student Service to validate admissionNo and get studentId and schoolId

        // Placeholder logic
        Long schoolId = 1L; // Should come from student service
        UUID studentId = 1L; // Should come from student service

        if (parentStudentLinkRepository.existsBySchoolIdAndAdmissionNo(schoolId, request.admissionNumber())) {
            throw new DuplicateResourceException("This child has already been linked to a parent account.");
        }

        ParentStudentLink link = ParentStudentLink.builder()
                .parentUser(parent)
                .schoolId(schoolId)
                .studentId(studentId)
                .admissionNo(request.admissionNumber())
                .build();

        parentStudentLinkRepository.save(link);
    }

    @Override
    public List<ChildDto> getChildren(String userEmail) {
        User parent = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return parentStudentLinkRepository.findAllByParentUser(parent).stream()
                .map(ParentStudentLinkMapper.INSTANCE::toChildDto)
                .collect(Collectors.toList());
    }
}