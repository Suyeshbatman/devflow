package com.devflow.user.config;

import com.devflow.user.model.User;
import com.devflow.user.repository.UserRepository;
import com.devflow.common.enums.ErrorCode;
import com.devflow.common.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @Configuration = this class defines Spring beans (@Bean methods)
// @EnableWebSecurity = activates Spring Security for this app
// @EnableMethodSecurity = allows @PreAuthorize("hasRole('ADMIN')")
//   annotations on individual controller methods
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserRepository userRepository;

    // ═══════════════════════════════════════════════════════
    // SECURITY FILTER CHAIN
    // This is the main security configuration
    // Every HTTP request passes through this chain of rules
    // ═══════════════════════════════════════════════════════
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                // Disable CSRF (Cross-Site Request Forgery) protection
                // CSRF is only needed for browser session-based apps
                // We use stateless JWT — no sessions, no CSRF risk
                .csrf(AbstractHttpConfigurer::disable)

                // Define which URLs need authentication
                .authorizeHttpRequests(auth -> auth
                        // These endpoints are PUBLIC — no token needed
                        // Anyone can register and login
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/actuator/health",  // API Gateway health checks
                                "/actuator/info"
                        ).permitAll()
                        // Every other endpoint requires a valid JWT token
                        // If no token → 401 Unauthorized
                        // If wrong role → 403 Forbidden
                        .anyRequest().authenticated()
                )

                // STATELESS = no HTTP sessions created or used
                // Every request must carry its own JWT token
                // This is essential for microservices — any instance
                // of user-service can handle any request because
                // state lives in the token, not the server
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Register our custom AuthenticationProvider
                // This tells Spring HOW to authenticate users
                // (load from DB → verify BCrypt password)
                .authenticationProvider(authenticationProvider())

                // Insert our JwtAuthFilter BEFORE Spring's default
                // UsernamePasswordAuthenticationFilter
                // This means: for every request, first check the JWT
                // header before doing anything else
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ═══════════════════════════════════════════════════════
    // USER DETAILS SERVICE
    // Tells Spring Security HOW to load a user from our DB
    // Spring calls this when it needs to verify a user exists
    // ═══════════════════════════════════════════════════════
    @Bean
    public UserDetailsService userDetailsService() {
        // Lambda implementation of UserDetailsService interface
        // Spring passes it the username (email in our case)
        // We load from DB and return — Spring handles the rest
        return email -> userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.USER_NOT_FOUND));
    }

    // ═══════════════════════════════════════════════════════
    // AUTHENTICATION PROVIDER
    // The component that actually performs authentication:
    // 1. Calls userDetailsService() to load user from DB
    // 2. Uses passwordEncoder() to verify the password
    // ═══════════════════════════════════════════════════════
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        // Tell it how to load users
        provider.setUserDetailsService(userDetailsService());
        // Tell it how passwords are encoded (BCrypt)
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ═══════════════════════════════════════════════════════
    // AUTHENTICATION MANAGER
    // The entry point for authentication
    // AuthService.login() calls authenticationManager.authenticate()
    // which internally uses our authenticationProvider()
    // ═══════════════════════════════════════════════════════
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ═══════════════════════════════════════════════════════
    // PASSWORD ENCODER
    // BCrypt is the industry standard for password hashing
    // It automatically salts passwords (prevents rainbow tables)
    // Strength 10 = 2^10 = 1024 hashing rounds (slow by design —
    // makes brute force attacks computationally expensive)
    // ═══════════════════════════════════════════════════════
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}