package com.devflow.userservice.config;

// Binds a group of YAML settings to a Java object.
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "devflow.jwt")
public record JwtProperties(
        String secret,
        Duration expiration,
        String issuer
) {

}
