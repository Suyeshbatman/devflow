package com.devflow.userservice.service;

import com.devflow.userservice.config.JwtProperties;
import com.devflow.userservice.entity.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
// JwsHeader: the token's HEADER part (which algorithm signed it).
import org.springframework.security.oauth2.jwt.JwsHeader;
// JwtClaimsSet: the token's PAYLOAD part (the claims).
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
// JwtEncoderParameters: header + claims bundled together for the encoder.
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

// Single responsibility: build and sign JWTs. Nothing else.
@Service
public class TokenService {
    private final JwtEncoder jwtEncoder;  // the bean from JwtConfig
    private final JwtProperties jwtProperties;  // issuer and expiration settings

    public TokenService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }
    // Creates a signed token for this user and returns it as a String ("eyJ...").
    public String generateToken(User user){
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())  // "iss": who made this token
                .issuedAt(now)                   // "iat": when it was made
                .expiresAt(now.plus(jwtProperties.expiration())) // "exp": now + 1 hour
                .subject(user.getId().toString())  // "sub": WHO it's about (the user id).
                .claim("email", user.getEmail())   // a custom claim for convenience
                .claim("roles", List.of(user.getRole().name())) // "roles": ["DEVELOPER"]. A LIST, so a user could have several roles later. .name() turns the enum into text.
                .build();

        // The HEADER: HS256 explicitly. Otherwise the encoder assumes RS256
        // (public/private key) and fails, because we only gave it a shared secret.
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        // Sign it: header + payload + secret → signature. Returns a Jwt object;
        // getTokenValue() gives the final "xxxxx.yyyyy.zzzzz" text.
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
