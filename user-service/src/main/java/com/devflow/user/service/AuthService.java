package com.devflow.user.service;

import com.devflow.common.enums.ErrorCode;
import com.devflow.common.exception.BaseException;
import com.devflow.user.dto.AuthResponse;
import com.devflow.user.dto.LoginRequest;
import com.devflow.user.dto.RegisterRequest;
import com.devflow.user.dto.UserDto;
import com.devflow.user.model.Role;
import com.devflow.user.model.User;
import com.devflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// @Service = Spring manages this as a singleton bean
// @RequiredArgsConstructor = Lombok generates a constructor for all
//   'private final' fields — this is constructor injection (best practice)
//   Spring sees the constructor and automatically injects the dependencies
// @Slf4j = Lombok creates a 'log' variable for logging
//   we can then do log.info("..."), log.error("...") anywhere in this class
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    // 'final' + @RequiredArgsConstructor = Spring injects these automatically
    // This is called Dependency Injection (DI) — we don't create these objects,
    // Spring creates them and hands them to us
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ═══════════════════════════════════════════════════════
    // REGISTER
    // ═══════════════════════════════════════════════════════

    // @Transactional = wraps this entire method in a database transaction
    // If anything fails midway (e.g. DB down), ALL changes are rolled back
    // No partial saves — either everything succeeds or nothing does
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Step 1: Check if email is already taken
        // If yes, throw our custom exception with the right ErrorCode
        // GlobalExceptionHandler catches this and returns a 409 response
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists: {}",
                    request.getEmail());
            throw new BaseException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // Step 2: Build the User entity from the RegisterRequest DTO
        // passwordEncoder.encode() runs BCrypt hashing:
        // "password123" → "$2a$10$N9qo8uLOickgx2ZMRZoSS..."
        // BCrypt is a one-way hash — we CANNOT reverse it
        // During login we hash the input and COMPARE hashes
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                // Default role for all new registrations is DEVELOPER
                // ADMIN role can only be assigned manually
                .role(Role.DEVELOPER)
                .enabled(true)
                .build();

        // Step 3: Save to PostgreSQL
        // JPA/Hibernate generates and runs:
        // INSERT INTO users (email, first_name, ...) VALUES (?, ?, ...)
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        // Step 4: Generate JWT tokens immediately
        // User is logged in right after registration — no need to log in again
        String accessToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        // Step 5: Build and return the auth response
        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    // ═══════════════════════════════════════════════════════
    // LOGIN
    // ═══════════════════════════════════════════════════════

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        try {
            // authenticationManager.authenticate() does two things:
            // 1. Loads the user from DB by email (via UserDetailsService)
            // 2. Compares the provided password against the stored BCrypt hash
            // If either fails, it throws BadCredentialsException
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),   // username (we use email)
                            request.getPassword() // raw password to verify
                    )
            );
        } catch (BadCredentialsException e) {
            // Don't tell the user whether it was wrong email OR wrong password
            // That would help attackers enumerate valid emails
            log.warn("Login failed for email: {}", request.getEmail());
            throw new BaseException(ErrorCode.INVALID_CREDENTIALS);
        }

        // If we reach here, authentication succeeded
        // Load the full user object to generate tokens
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("Login successful for user id: {}", user.getId());
        return buildAuthResponse(user, accessToken, refreshToken);
    }

    // ═══════════════════════════════════════════════════════
    // GET CURRENT USER PROFILE
    // ═══════════════════════════════════════════════════════

    // @Transactional(readOnly=true) = tells Hibernate this is a read-only
    // operation — it can optimize by skipping dirty checking
    // (dirty checking = Hibernate tracking changes to entities)
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String email) {
        log.info("Fetching profile for: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // Convert entity → DTO (never return the raw entity to clients)
        return mapToDto(user);
    }

    // ═══════════════════════════════════════════════════════
    // REFRESH TOKEN
    // ═══════════════════════════════════════════════════════

    public AuthResponse refreshToken(String refreshToken) {
        // Extract email from the refresh token
        String email = jwtService.extractEmail(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ErrorCode.USER_NOT_FOUND));

        // Validate the refresh token is still valid
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BaseException(ErrorCode.INVALID_TOKEN);
        }

        // Issue a brand new access token
        String newAccessToken = jwtService.generateToken(user);

        log.info("Token refreshed for user: {}", email);
        return buildAuthResponse(user, newAccessToken, refreshToken);
    }

    // ═══════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════

    // Builds the AuthResponse from a User + tokens
    // Extracted into a method to avoid repeating this in
    // register(), login(), and refreshToken()
    private AuthResponse buildAuthResponse(User user,
                                           String accessToken,
                                           String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .expiresIn(86400000L) // 24 hours in milliseconds
                .build();
    }

    // Converts User entity → UserDto (safe, no password)
    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}