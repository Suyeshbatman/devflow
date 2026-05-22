package com.devflow.user.config;

import com.devflow.user.model.User;
import com.devflow.user.repository.UserRepository;
import com.devflow.user.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

// @Component = Spring manages this as a bean
// OncePerRequestFilter = Spring guarantees this filter runs
//   EXACTLY once per request (not multiple times)
// @Slf4j = gives us the 'log' variable for logging
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    // This method runs on EVERY incoming HTTP request
    // @NonNull = Spring will never pass null values here
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain   // the rest of the filter chain
    ) throws ServletException, IOException {

        // Step 1: Read the Authorization header
        // Valid format: "Bearer eyJhbGciOiJIUzI1NiJ9.eyJ..."
        final String authHeader = request.getHeader("Authorization");

        // Step 2: If no header or doesn't start with "Bearer ",
        // skip JWT processing entirely — pass to next filter
        // SecurityConfig will handle whether auth is required
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract the token part after "Bearer "
        // "Bearer eyJ..." → "eyJ..."
        final String jwt = authHeader.substring(7);

        // Step 4: Extract the email from inside the token
        // If token is malformed, jwtService throws an exception
        final String userEmail;
        try {
            userEmail = jwtService.extractEmail(jwt);
        } catch (Exception e) {
            log.warn("Failed to extract email from JWT: {}", e.getMessage());
            // Don't block the request — let it through
            // SecurityConfig will reject it if the endpoint needs auth
            filterChain.doFilter(request, response);
            return;
        }

        // Step 5: Only proceed if we got an email AND
        // the user isn't already authenticated in this request
        // (SecurityContextHolder.getContext().getAuthentication() == null
        //  means: not yet authenticated in this request's lifecycle)
        if (userEmail != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

            // Step 6: Load the actual user from database
            Optional<User> userOptional = userRepository.findByEmail(userEmail);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // Step 7: Validate the token against this user
                // Checks: correct email + not expired + valid signature
                if (jwtService.isTokenValid(jwt, user)) {

                    // Step 8: Create an Authentication object
                    // UsernamePasswordAuthenticationToken with 3 args =
                    // "this user is authenticated"
                    // getAuthorities() = ["ROLE_DEVELOPER"] etc.
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,                    // principal (who they are)
                                    null,                    // credentials (null = already verified)
                                    user.getAuthorities()    // roles/permissions
                            );

                    // Attach request details (IP address, session id etc.)
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // Step 9: Store authentication in SecurityContext
                    // This is how Spring Security knows "this request
                    // is authenticated as user X with role Y"
                    // Controllers can then call:
                    // SecurityContextHolder.getContext().getAuthentication()
                    // to get the current user
                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);

                    log.debug("Authenticated user: {}", userEmail);
                }
            }
        }

        // Step 10: Pass request to the next filter / controller
        filterChain.doFilter(request, response);
    }
}