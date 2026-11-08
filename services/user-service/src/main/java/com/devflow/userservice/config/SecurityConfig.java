package com.devflow.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
// HttpSecurity: the builder used to configure the security filter chain.
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// STATELESS = never create HTTP sessions (we use tokens instead).
import org.springframework.security.config.http.SessionCreationPolicy;
// Converts a validated JWT into Spring's "Authentication" object (who the user is + their authorities).
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
// Reads authorities (roles) out of a claim in the JWT.
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
// The finished chain of security filters.
import org.springframework.security.web.SecurityFilterChain;

// The rules: which URLs are public, which need a login, and which need a specific role.
@Configuration
public class SecurityConfig {
    // "throws Exception": http.build() may throw a "checked" exception, and Java forces us
    // to either catch it or declare it. Declaring it lets Spring deal with it at startup.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Each "csrf -> ..." is a LAMBDA: we receive a configurer object and set it up.
                // CSRF protection defends COOKIE-based logins. We use Bearer headers, so we disable it.
                .csrf(csrf -> csrf.disable())

                // Never create or use an HTTP session. Every request must carry its own token.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // URL RULES, checked TOP TO BOTTOM. The FIRST matching rule wins, so order matters!
                .authorizeHttpRequests(auth -> auth
                        // Public: anyone may register or log in (you can't have a token before logging in).
                        .requestMatchers(HttpMethod.POST, "/api/users/register", "/api/auth/login").permitAll()
                        // Public: health checks (Docker/Kubernetes call these without a token).
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        // Public: Spring's internal error page. Without this, some errors
                        // (like an invalid UUID → 400) would wrongly turn into 401.
                        .requestMatchers("/error").permitAll()
                        // Any logged-in user can see their OWN profile.
                        // This MUST come before the next rule, because "/api/users/*" would also match "/api/users/me".
                        .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                        // Looking up ANY user by id: admins only (RBAC).
                        // "*" matches exactly one path segment, e.g. /api/users/8c1f...
                        .requestMatchers(HttpMethod.GET, "/api/users/*").hasRole("ADMIN")
                        // Everything else: any valid token. Secure by default, so new endpoints are never public by accident.
                        .anyRequest().authenticated()
                )

                // Turn on JWT authentication: read "Authorization: Bearer <token>",
                // verify it with our JwtDecoder bean, then convert it using OUR converter (below).
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        // Assemble everything into the final filter chain.
        return http.build();
    }

    // By default, Spring reads authorities from a "scope" claim and prefixes them with "SCOPE_".
    // Our tokens carry a "roles" claim instead, and we want the "ROLE_" prefix, so hasRole("ADMIN") works.
    // Example: token "roles": ["ADMIN"] → authority "ROLE_ADMIN".
    // "private" and not a @Bean: it's only a helper used inside this class.
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("roles"); // which claim to read
        authoritiesConverter.setAuthorityPrefix("ROLE_");      // what to put in front of each value

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}
