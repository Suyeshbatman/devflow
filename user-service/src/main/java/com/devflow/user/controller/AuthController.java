package com.devflow.user.controller;

import com.devflow.common.dto.ApiResponse;
import com.devflow.user.dto.AuthResponse;
import com.devflow.user.dto.LoginRequest;
import com.devflow.user.dto.RegisterRequest;
import com.devflow.user.dto.UserDto;
import com.devflow.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

// @RestController = @Controller + @ResponseBody
//   Every method return value is automatically serialized to JSON
// @RequestMapping = base URL path for all endpoints in this class
// @RequiredArgsConstructor = Lombok injects AuthService via constructor
// @Slf4j = gives us 'log'
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    // ═══════════════════════════════════════════════════════
    // POST /api/auth/register
    // Public endpoint — no JWT required
    //
    // Request body:
    // {
    //   "firstName": "John",
    //   "lastName": "Doe",
    //   "email": "john@devflow.com",
    //   "password": "securepass123"
    // }
    //
    // @Valid = tells Spring to run all validation annotations
    //   on RegisterRequest before this method even runs
    //   If validation fails → GlobalExceptionHandler catches it
    // @RequestBody = deserializes the JSON body into RegisterRequest
    // ResponseEntity = lets us control the HTTP status code
    // ═══════════════════════════════════════════════════════
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Register request received for: {}", request.getEmail());

        AuthResponse authResponse = authService.register(request);

        // HTTP 201 Created (not 200 OK) — semantically correct
        // for resource creation
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Registration successful", authResponse));
    }

    // ═══════════════════════════════════════════════════════
    // POST /api/auth/login
    // Public endpoint — no JWT required
    //
    // Request body:
    // {
    //   "email": "john@devflow.com",
    //   "password": "securepass123"
    // }
    // ═══════════════════════════════════════════════════════
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request received for: {}", request.getEmail());

        AuthResponse authResponse = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", authResponse));
    }

    // ═══════════════════════════════════════════════════════
    // POST /api/auth/refresh
    // Public endpoint — sends the refresh token, gets new access token
    //
    // Request header:
    // Authorization: Bearer <refresh_token>
    // ═══════════════════════════════════════════════════════
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader("Authorization") String authHeader) {

        // Strip "Bearer " prefix to get raw token
        String refreshToken = authHeader.substring(7);
        AuthResponse authResponse = authService.refreshToken(refreshToken);

        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed", authResponse));
    }

    // ═══════════════════════════════════════════════════════
    // GET /api/users/me
    // PROTECTED — requires valid JWT
    //
    // @AuthenticationPrincipal = Spring injects the currently
    //   authenticated user (set by JwtAuthFilter in SecurityContext)
    //   We get the full User object without a DB call
    // ═══════════════════════════════════════════════════════
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        UserDto userDto = authService.getCurrentUser(
                userDetails.getUsername()); // getUsername() returns email

        return ResponseEntity.ok(
                ApiResponse.success("User profile retrieved", userDto));
    }
}