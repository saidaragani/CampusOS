package com.campusos.common_lib.security;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Loads RSA keys from PEM text. Private key must be PKCS#8 ({@code BEGIN PRIVATE KEY}),
 * public key X.509 ({@code BEGIN PUBLIC KEY}). Shared by auth (signing) and gateway (verifying).
 */
public final class RsaKeys {

    private RsaKeys() {
    }

    public static PrivateKey privateKeyFromPem(String pem) {
        try {
            byte[] der = Base64.getDecoder().decode(stripPem(pem));
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load RSA private key", ex);
        }
    }

    public static PublicKey publicKeyFromPem(String pem) {
        try {
            byte[] der = Base64.getDecoder().decode(stripPem(pem));
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load RSA public key", ex);
        }
    }

    private static String stripPem(String pem) {
        return pem.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
    }
}
