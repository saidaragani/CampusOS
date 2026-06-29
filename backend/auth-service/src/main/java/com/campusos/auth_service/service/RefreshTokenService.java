package com.campusos.auth_service.service;

import com.campusos.auth_service.entity.RefreshToken;
import com.campusos.auth_service.entity.User;

public interface RefreshTokenService {

    /**
     * Issues a fresh refresh token for the user, replacing any existing one
     * (one active refresh token per user).
     */
    RefreshToken create(User user);

    /**
     * Validates an existing refresh token and rotates it: the presented token is
     * consumed and a brand-new token is issued for the same user.
     *
     * @throws com.campusos.auth_service.exception.ApiException if the token is
     *         unknown, expired or revoked.
     */
    RefreshToken verifyAndRotate(String token);

    /**
     * Revokes a single refresh token (logout). No-op if the token is unknown.
     */
    void revoke(String token);

    /**
     * Revokes all refresh tokens for a user (e.g. after a password reset).
     */
    void revokeAllForUser(User user);
}
