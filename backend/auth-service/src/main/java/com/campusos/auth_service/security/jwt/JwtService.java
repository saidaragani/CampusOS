package com.campusos.auth_service.security.jwt;

import com.campusos.auth_service.security.User.UserPrincipal;
import com.campusos.common_lib.security.RsaKeys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Issues + validates access tokens using RS256. auth-service holds the private
 * key (signing) and the public key (validating its own inbound Feign calls).
 */
@Service
public class JwtService {

    @Value("${jwt.private-key-location:classpath:keys/private_key.pem}")
    private Resource privateKeyResource;

    @Value("${jwt.public-key-location:classpath:keys/public_key.pem}")
    private Resource publicKeyResource;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    void init() {
        this.privateKey = RsaKeys.privateKeyFromPem(read(privateKeyResource));
        this.publicKey = RsaKeys.publicKeyFromPem(read(publicKeyResource));
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpiration;
    }

    public String generateToken(UserPrincipal userPrincipal) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userPrincipal.getUserId().toString());

        UUID schoolId = userPrincipal.getSchoolId();
        if (schoolId != null) {
            claims.put("schoolId", schoolId.toString());
        }
        claims.put("fullName", userPrincipal.getUser().getFullName());
        claims.put("role", userPrincipal.getUser().getRole().getName().name());

        return Jwts.builder()
                .claims(claims)
                .subject(userPrincipal.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaims(token).get("userId", String.class));
    }

    public UUID extractSchoolId(String token) {
        String schoolId = extractClaims(token).get("schoolId", String.class);
        return schoolId == null ? null : UUID.fromString(schoolId);
    }

    public String extractDisplayName(String token) {
        return extractClaims(token).get("fullName", String.class);
    }

    private String read(Resource resource) {
        try {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read RSA key resource: " + resource, ex);
        }
    }
}
