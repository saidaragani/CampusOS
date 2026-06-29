package com.campusos.auth_service.serviceimpl;

import com.campusos.auth_service.entity.RefreshToken;
import com.campusos.auth_service.entity.User;
import com.campusos.auth_service.exception.BadRequestException;
import com.campusos.auth_service.repository.RefreshTokenRepository;
import com.campusos.auth_service.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMs;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public RefreshToken create(User user) {
        // One active refresh token per user: drop any existing one first.
        refreshTokenRepository.deleteByUser(user);

        RefreshToken token = RefreshToken.builder()
                .token(generateTokenString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusNanos(refreshTokenExpirationMs * 1_000_000))
                .revoked(false)
                .build();

        return refreshTokenRepository.saveAndFlush(token);
    }

    @Override
    @Transactional
    public RefreshToken verifyAndRotate(String token) {
        RefreshToken existing = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (!existing.isValid()) {
            // Defensively revoke and reject.
            existing.setRevoked(true);
            refreshTokenRepository.save(existing);
            throw new BadRequestException("Refresh token expired or revoked. Please log in again.");
        }

        User user = existing.getUser();
        // Rotation: create() removes the consumed token and issues a new one.
        return create(user);
    }

    @Override
    @Transactional
    public void revoke(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    @Override
    @Transactional
    public void revokeAllForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    private String generateTokenString() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
