package com.devflow.marketplace.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "devflow.jwt")
public record JwtProperties (
        String secret,
        String issuer
){

}
