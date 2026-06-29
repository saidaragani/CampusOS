package com.campusos.api_gateway.security;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


/**
 * RSA public-key loader.kept local to the gateway (rather than pulled from
 * common-lib) so the gateway stays a thin edge service with no JPA/datasource on
 * its classpath. The gatway only the needs the public key to VERIFY tokens.
 */
public class RsaKeys {

    private RsaKeys() {
    }
    public static PublicKey publicKeyFromPem(String pem){
        try{
            String body = pem.replaceAll("-----BEGIN [^-]+-----","")
                    .replaceAll("-----END [^-]+-----","")
                    .replaceAll("\\s","");
            byte[] der = Base64.getDecoder().decode(body);
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create RSA public key from PEM: " + pem, ex);
        }
    }
}
