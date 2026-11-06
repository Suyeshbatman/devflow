package com.devflow.userservice.service;

import com.devflow.userservice.dto.RegisterRequest;
import com.devflow.userservice.dto.UserResponse;
import com.devflow.userservice.entity.Role;
import com.devflow.userservice.entity.User;
import com.devflow.userservice.exception.EmailAlreadyExistsException;
import com.devflow.userservice.exception.UserNotFoundException;
import com.devflow.userservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
// @Service: like @Component (creates a bean), but labels this class as business logic.
import org.springframework.stereotype.Service;
// Spring's @Transactional (prefer this one over the jakarta.transaction version).
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
public class UserService {
    //Dependency this service needs
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //Register a new user, use @transactional = all or nothing, if anything throws, every db change in this method is rolled back//
    @Transactional
    public UserResponse register(RegisterRequest request){
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if(userRepository.existsByEmail(email)){
            throw new EmailAlreadyExistsException(email);
        }
        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(email, passwordHash, request.fullName().trim(), Role.DEVELOPER);
        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(UUID id){
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}