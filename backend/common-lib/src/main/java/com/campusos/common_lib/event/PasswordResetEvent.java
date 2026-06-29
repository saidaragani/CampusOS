package com.campusos.common_lib.event;

/** Published by auth-service on forgot-password (messaging emails the reset token). */
public record PasswordResetEvent(
        String email,
        String fullName,
        String resetToken
) {}
