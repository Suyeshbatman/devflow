package com.devflow.userservice.service;

import com.devflow.userservice.config.JwtProperties;
import com.devflow.userservice.dto.LoginRequest;
import com.devflow.userservice.dto.LoginResponse;
import com.devflow.userservice.entity.User;
import com.devflow.userservice.exception.InvalidCredentialsException;
import com.devflow.userservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

// Handles logging in: checks the credentials, then issues a token.
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final JwtProperties jwtProperties;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService, JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public LoginResponse login(LoginRequest request){
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if(!passwordEncoder.matches(request.password(), user.getPasswordHash())){
            throw new InvalidCredentialsException();
        }
        if(!user.isEnabled()){
            throw new InvalidCredentialsException();
        }

        user.recordSuccessfulLogin();

        String token = tokenService.generateToken(user);
        return new LoginResponse(token, "Bearer", jwtProperties.expiration().toSeconds());
    }
}
