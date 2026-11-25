package com.devflow.marketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

// SecretKey: an INTERFACE; SecretKeySpec: a CLASS implementing it; Base64: a CLASS.
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {
    // [FIELD] The injected settings OBJECT. private + final.
    private final JwtProperties jwtProperties;

    // [CONSTRUCTOR] Spring injects the JwtProperties bean (constructor injection).
    public JwtConfig(JwtProperties jwtProperties){
        this.jwtProperties = jwtProperties;
    }

    @Bean
    // [METHOD] Builds the decoder
    public JwtDecoder jwtDecoder() {
        // [LOCAL VARIABLE] [STATIC METHOD CALL] Base64.getDecoder() → [METHOD CALL] .decode(...) → raw key bytes.
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.secret());

        // [KEYWORD] if: refuse to start with a key shorter than 256 bits.
        if (keyBytes.length < 32) {
            // [KEYWORD] throw + [CONSTRUCTOR CALL] new IllegalStateException(...)
            throw new IllegalStateException(
                    "devflow.jwt.secret must be at least 32 bytes (256 bits) after Base64 decoding");
        }
        // [LOCAL VARIABLE] + [CONSTRUCTOR CALL] Wrap the bytes as an HMAC-SHA256 key OBJECT.
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");

        // [LOCAL VARIABLE] Builder pattern: [STATIC METHOD CALL] withSecretKey(...) → [METHOD CALL]
        //   macAlgorithm(...) with the [CONSTANT] HS256 → [METHOD CALL] build().
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        // [METHOD CALL] Check expiry AND issuer on every token.
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()));

        // [KEYWORD] return the decoder OBJECT to Spring.
        return decoder;
    }

}
