package com.campusos.auth_service.serviceimpl;

import com.campusos.auth_service.entity.RefreshToken;
import com.campusos.auth_service.entity.User;
import com.campusos.auth_service.exception.BadRequestException;
import com.campusos.auth_service.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @InjectMocks private RefreshTokenServiceImpl refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationMs", 604_800_000L);
        user = User.builder().id(UUID.randomUUID()).email("u@campusos.com").build();
    }

    @Test
    void create_replacesExistingAndIssuesNewToken() {
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class)))
                .thenAnswer(i -> i.getArgument(0));

        RefreshToken token = refreshTokenService.create(user);

        verify(refreshTokenRepository).deleteByUser(user);
        verify(refreshTokenRepository).saveAndFlush(any(RefreshToken.class));
        assertThat(token.getToken()).isNotBlank();
        assertThat(token.getRevoked()).isFalse();
        assertThat(token.getExpiryDate()).isAfter(LocalDateTime.now());
        assertThat(token.getUser()).isEqualTo(user);
    }

    @Test
    void verifyAndRotate_validToken_rotates() {
        RefreshToken existing = RefreshToken.builder()
                .token("old")
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("old")).thenReturn(Optional.of(existing));
        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class)))
                .thenAnswer(i -> i.getArgument(0));

        RefreshToken rotated = refreshTokenService.verifyAndRotate("old");

        assertThat(rotated.getToken()).isNotBlank();
        assertThat(rotated.getToken()).isNotEqualTo("old");
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    void verifyAndRotate_unknownToken_throws() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.verifyAndRotate("missing"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void verifyAndRotate_expiredToken_throwsAndRevokes() {
        RefreshToken expired = RefreshToken.builder()
                .token("old")
                .user(user)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("old")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenService.verifyAndRotate("old"))
                .isInstanceOf(BadRequestException.class);

        assertThat(expired.getRevoked()).isTrue();
        verify(refreshTokenRepository).save(expired);
    }

    @Test
    void revoke_marksTokenRevoked() {
        RefreshToken token = RefreshToken.builder()
                .token("tok")
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("tok")).thenReturn(Optional.of(token));

        refreshTokenService.revoke("tok");

        assertThat(token.getRevoked()).isTrue();
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void revoke_unknownToken_isNoOp() {
        when(refreshTokenRepository.findByToken("nope")).thenReturn(Optional.empty());

        refreshTokenService.revoke("nope");

        verify(refreshTokenRepository, never()).save(any());
    }
}
