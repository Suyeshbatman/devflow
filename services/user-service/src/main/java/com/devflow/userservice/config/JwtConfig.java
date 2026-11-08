package com.devflow.userservice.config;

// Nimbus is the JWT library Spring Security uses internally.
// ImmutableSecret wraps our key into the form Nimbus expects (a "key source").
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
// MacAlgorithm.HS256: the HMAC-SHA256 signing algorithm.
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
// JwtDecoder: reads and VERIFIES incoming tokens. JwtEncoder: CREATES signed tokens.
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
// Ready-made validation rules (expiry, issuer...).
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;              // Java's type for symmetric (shared) secret keys
import javax.crypto.spec.SecretKeySpec;     // builds a SecretKey from raw bytes
import java.util.Base64;                    // Java's built-in Base64 encoder/decoder

// Creates the beans needed to SIGN tokens (encoder) and VERIFY them (decoder).
@Configuration
public class JwtConfig {
    private final JwtProperties jwtProperties;

    // Spring injects the JwtProperties bean, already filled from application.yaml.
    public JwtConfig (JwtProperties jwtProperties){
        this.jwtProperties = jwtProperties;
    }

    @Bean
    public SecretKey jwtSecretKey(){
        // Turn the Base64 text from the YAML back into raw bytes.
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.secret());

        // Fail fast: refuse to START with a weak key, rather than failing at the first login.
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "devflow.jwt.secret must be at least 32 bytes (256 bits) after Base64 decoding");
        }
        // Wrap the bytes as a key for the "HmacSHA256" algorithm (the HS256 used by JWT).
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    // The ENCODER: creates and signs tokens at login.
    // The method parameter "SecretKey jwtSecretKey" is injected by Spring: the bean defined above.
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        // "new ImmutableSecret<>(...)": the <> is the "diamond operator", which lets Java
        // infer the generic type itself, so we don't have to write it out.
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    // The DECODER: on every request, it checks that the token's signature is valid,
    // that it isn't expired, and that the issuer matches.
    // Spring Security uses this bean automatically for incoming Bearer tokens.
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        // Builder pattern: configure step by step, then .build().
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)   // accept ONLY HS256-signed tokens
                .build();

        // The default validators check exp (expiry) and nbf ("not before");
        // "WithIssuer" ALSO requires iss == "devflow-user-service".
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(jwtProperties.issuer()));
        return decoder;
    }
}
