package com.devflow.marketplace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
// The marketplace's security rules.
public class SecurityConfig {

    // The returned OBJECT is the active security filter chain.
    @Bean
    // [METHOD] [PARAMETER] http: an HttpSecurity OBJECT injected by Spring.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // [METHOD CALL] + [LAMBDA] No CSRF (Bearer tokens, no cookies).
                .csrf(csrf -> csrf.disable())
                // [METHOD CALL] + [LAMBDA] Never create sessions; the [CONSTANT] STATELESS.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // [METHOD CALL] + [LAMBDA] URL rules, top to bottom, first match wins.
                .authorizeHttpRequests(auth -> auth
                        // Browsing the catalog is PUBLIC: anyone can look at products (like any online store).
                        //   Both patterns are listed so "/api/products" itself is clearly included.
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
                        // Health checks are public.
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        // Spring's error page is public, so 404/400 errors aren't turned into 401.
                        .requestMatchers("/error").permitAll()
                        // Everything else (creating products, placing orders...) requires a valid token.
                        .anyRequest().authenticated()
                )
                // [METHOD CALL] Enable JWT authentication, using our converter so roles become ROLE_*.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

        // return + [METHOD CALL] build() → the SecurityFilterChain OBJECT.
        return http.build();
    }

    // [METHOD] private helper (not a bean): the "roles" claim → "ROLE_" authorities.
    //   Needed in Step 2.2 for rules like "only ADMIN can delete any product".
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        // [LOCAL VARIABLE] + [CONSTRUCTOR CALL]
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        // [METHOD CALL] Read authorities from the "roles" claim...
        authoritiesConverter.setAuthoritiesClaimName("roles");
        // [METHOD CALL] ...and prefix each one: "ADMIN" → "ROLE_ADMIN".
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        // [LOCAL VARIABLE] + [CONSTRUCTOR CALL]
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        // [METHOD CALL] Plug the authorities converter in.
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }
}