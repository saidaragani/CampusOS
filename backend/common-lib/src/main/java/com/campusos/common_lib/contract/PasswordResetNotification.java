package com.campusos.common_lib.contract;

/**
 * Shared contract handed to message-service to email a password-reset token.
 * The raw (un-hashed) token travels here; the issuing service stores only its hash.
 */
public record PasswordResetNotification(
        String email,
        String fullName,
        String resetToken
) {}
