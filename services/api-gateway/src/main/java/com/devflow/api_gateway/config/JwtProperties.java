package com.devflow.api_gateway.config;

//@ConfigurationProperties is an ANNOTATION that binds YAML settings to a Java type.
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devflow.jwt")
// An immutable data holder. Java auto-generates its CONSTRUCTOR and
// the accessor METHODS secret() and issuer().
// The gateway only VERIFIES tokens and never creates them, so it doesn't need "expiration".
public record JwtProperties(
        String secret,  //the shared Base64 secret key (becomes a private final field)
        String issuer   //the expected "iss" claim
) {

}
