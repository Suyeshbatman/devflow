package com.devflow.userservice.dto;

public record LoginResponse(
        String accessToken, // the JWT itself
        String tokenType,   // always "Bearer": tells the client how to send it
        long expiresIn      // seconds until it expires
) {
}
