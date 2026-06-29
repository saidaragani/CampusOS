package com.campusos.api_gateway.security;

import com.campusos.api_gateway.constants.AuthHeaders;
import com.campusos.api_gateway.web.MutableHttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Single JWT-validation point for the whole platform. Validates the access token
 * at the edge, then forwards trusted identity headers ({@link AuthHeaders}) to the
 * downstream service so services don't each re-validate. Public endpoints pass
 * through without a token (with any client-supplied identity headers stripped).
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@RequiredArgsConstructor
public class AuthGatewayFilter extends OncePerRequestFilter {

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/register/**",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/actuator/health",
            "/actuator/info"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Public endpoints: forward, but never trust client-supplied identity headers.
        if (isPublic(request.getRequestURI())) {
            filterChain.doFilter(stripIdentityHeaders(request), response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            unauthorized(response, "Missing or invalid Authorization header");
            return;
        }

        Claims claims;
        try {
            claims = jwtService.validateAndGetClaims(authHeader.substring(7));
        } catch (Exception ex) {
            unauthorized(response, "Invalid or expired token");
            return;
        }

        MutableHttpServletRequest mutable = stripIdentityHeaders(request);
        mutable.setHeader(AuthHeaders.EMAIL, claims.getSubject());
        putIfPresent(mutable, AuthHeaders.USER_ID, claims.get("userId", String.class));
        putIfPresent(mutable, AuthHeaders.ROLE, claims.get("role", String.class));
        putIfPresent(mutable, AuthHeaders.SCHOOL_ID, claims.get("schoolId", String.class));

        filterChain.doFilter(mutable, response);
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private MutableHttpServletRequest stripIdentityHeaders(HttpServletRequest request) {
        MutableHttpServletRequest mutable = new MutableHttpServletRequest(request);
        mutable.removeHeader(AuthHeaders.USER_ID);
        mutable.removeHeader(AuthHeaders.EMAIL);
        mutable.removeHeader(AuthHeaders.ROLE);
        mutable.removeHeader(AuthHeaders.SCHOOL_ID);
        return mutable;
    }

    private void putIfPresent(MutableHttpServletRequest request, String name, String value) {
        if (value != null && !value.isBlank()) {
            request.setHeader(name, value);
        }
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                Map.of("success", false, "message", message));
    }
}
