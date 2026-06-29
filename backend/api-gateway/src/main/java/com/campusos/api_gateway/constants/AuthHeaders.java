package com.campusos.api_gateway.constants;

/**
 * Trusted identity headers the gateway injects after validating the JWT.
 * Downstream services read these instead of re-validating the token. The gateway
 * always strips any client-supplied values for these names before injecting, so
 * they cannot be spoofed from outside.
 */
public final class AuthHeaders {

    private AuthHeaders() {
    }

    public static final String USER_ID = "X-Auth-User-Id";
    public static final String EMAIL = "X-Auth-Email";
    public static final String ROLE = "X-Auth-Role";
    public static final String SCHOOL_ID = "X-Auth-School-Id";
}
