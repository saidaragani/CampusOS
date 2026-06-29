package com.campusos.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;

@Component
public class AuthGatewayFilter implements GatewayFilter {

    @Value("${jwt.secret}")
    private String secret;

    private final List<String> publicEndpoints = List.of(
            "/api/auth/login",
            "/api/auth/register/parent",
            "/api/auth/forgot-password",
            "/api/auth/reset-password"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 1. Allow public endpoints to pass through without a token
        if (publicEndpoints.stream().anyMatch(p -> request.getURI().getPath().endsWith(p))) {
            return chain.filter(exchange);
        }

        // 2. Check for the Authorization header on protected endpoints
        if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        // 3. Validate the token and extract claims
        try {
            String token = authHeader.substring(7);
            byte[] keyBytes = secret.getBytes();
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);

            Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();

            // 4. Mutate the request to add the trusted X-Auth headers
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-Auth-User-Id", claims.get("userId", String.class))
                    .header("X-Auth-User-Email", claims.getSubject())
                    .header("X-Auth-Role", claims.get("role", String.class))
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        } catch (Exception e) {
            // Log error if needed
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}