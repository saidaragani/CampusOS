package com.campusos.notification_service.security;

/** Trusted identity headers injected by the api-gateway after it validates the JWT. */
public final class AuthHeaders {

    private AuthHeaders() {
    }

    public static final String USER_ID = "X-Auth-User-Id";
    public static final String EMAIL = "X-Auth-Email";
    public static final String ROLE = "X-Auth-Role";
    public static final String SCHOOL_ID = "X-Auth-School-Id";
}
