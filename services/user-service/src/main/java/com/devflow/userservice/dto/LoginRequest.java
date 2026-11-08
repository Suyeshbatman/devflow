package com.devflow.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// The JSON body for POST /api/auth/login:  { "email": "...", "password": "..." }
public record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,
        @NotBlank(message = "Password is required")
        String password
) {
}
