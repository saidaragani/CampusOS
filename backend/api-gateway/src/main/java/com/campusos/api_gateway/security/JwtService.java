package com.campusos.api_gateway.security;

import com.campusos.common_lib.security.RsaKeys;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;

/**
 * The gateway only VERIFIES access tokens (RS256) using the platform's public
 * key. It never signs — that's auth-service's job (which holds the private key).
 */
@Component
public class JwtService {

    @Value("${jwt.public-key-location:classpath:keys/public_key.pem}")
    private Resource publicKeyResource;

    private PublicKey publicKey;

    @PostConstruct
    void init() {
        this.publicKey = RsaKeys.publicKeyFromPem(read(publicKeyResource));
    }

    /**
     * @throws io.jsonwebtoken.JwtException if the token is malformed, tampered or expired.
     */
    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String read(Resource resource) {
        try {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to read RSA public key: " + resource, ex);
        }
    }
}
