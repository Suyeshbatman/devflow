package com.devflow.user.dto;

import com.devflow.user.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// What we send BACK after successful login or registration
// Contains the JWT token the client must store and send
// with every future request
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    // The JWT access token
    // Client sends this in every request header:
    // Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
    private String accessToken;

    // Longer-lived token to get a new accessToken
    // when it expires (without logging in again)
    private String refreshToken;

    // Token type — always "Bearer" for JWT
    @Builder.Default
    private String tokenType = "Bearer";

    // Basic user info so the frontend can display it
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;

    // When does the access token expire? (milliseconds)
    private long expiresIn;
}