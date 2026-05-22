package com.devflow.gateway.filter;

import com.devflow.gateway.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    // Public paths — no JWT required
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        log.debug("Gateway request: {} {}", request.getMethod(), path);

        // Skip JWT check for public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Check Authorization header exists
        String authHeader = request.getHeaders()
                .getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing Authorization header for: {}", path);
            return rejectRequest(exchange, HttpStatus.UNAUTHORIZED);
        }

        // Extract and validate JWT
        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Invalid or expired JWT for: {}", path);
            return rejectRequest(exchange, HttpStatus.UNAUTHORIZED);
        }

        // Extract user info from token
        String email = jwtUtil.extractEmail(token);
        String role = jwtUtil.extractRole(token);

        log.debug("Authenticated: {} ({})", email, role);

        // Add user info as headers — downstream services
        // read these instead of validating JWT themselves
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Email", email)
                .header("X-User-Role", role)
                .build();

        return chain.filter(
                exchange.mutate()
                        .request(mutatedRequest)
                        .build());
    }

    // Run this filter first — before everything else
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
                .anyMatch(path::startsWith);
    }

    private Mono<Void> rejectRequest(ServerWebExchange exchange,
                                     HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders()
                .add("Content-Type", "application/json");
        return response.setComplete();
    }
}