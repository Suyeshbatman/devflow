package com.devflow.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
//The gateway's security rules: which requests may pass through without a token.
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        //   login and registration pass through WITHOUT a token (HttpMethod.POST is a CONSTANT).
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/users/register").permitAll()
                        // The gateway's own health endpoint is public.
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        // Spring's internal error page is public, so errors aren't turned into 401s.
                        .requestMatchers("/error").permitAll()
                        //.anyRequest().authenticated(): EVERYTHING else needs a valid token.
                        // No role checks here: role rules belong to each service (the User Service checks ADMIN).
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

}
