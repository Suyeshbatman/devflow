package com.devflow.user.service;

import com.devflow.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // ═══════════════════════════════════════════════════════
    // PUBLIC API
    // ═══════════════════════════════════════════════════════

    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getId());
        extraClaims.put("firstName", user.getFirstName());
        extraClaims.put("lastName", user.getLastName());
        return buildToken(extraClaims, user, jwtExpiration);
    }

    public String generateRefreshToken(User user) {
        return buildToken(new HashMap<>(), user, refreshExpiration);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, User user) {
        final String email = extractEmail(token);
        return email.equals(user.getEmail()) && !isTokenExpired(token);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // ═══════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════

    private String buildToken(Map<String, Object> extraClaims,
                              User user,
                              long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                // .claims() replaces .setClaims() in jjwt 0.12.x
                .subject(user.getEmail())
                // .subject() replaces .setSubject() in jjwt 0.12.x
                .issuedAt(new Date(System.currentTimeMillis()))
                // .issuedAt() replaces .setIssuedAt() in jjwt 0.12.x
                .expiration(new Date(System.currentTimeMillis() + expiration))
                // .expiration() replaces .setExpiration() in jjwt 0.12.x
                .signWith(getSigningKey())
                // signWith(key) — algorithm auto-detected from key type
                // no need to pass SignatureAlgorithm.HS256 separately
                .compact();
    }

    private <T> T extractClaim(String token,
                               Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                // .parser() replaces .parserBuilder() in jjwt 0.12.x
                .verifyWith(getSigningKey())
                // .verifyWith() replaces .setSigningKey() in jjwt 0.12.x
                .build()
                .parseSignedClaims(token)
                // .parseSignedClaims() replaces .parseClaimsJws() in jjwt 0.12.x
                .getPayload();
        // .getPayload() replaces .getBody() in jjwt 0.12.x
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
        // Returns SecretKey instead of Key
        // SecretKey is more specific — required by new jjwt API
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}