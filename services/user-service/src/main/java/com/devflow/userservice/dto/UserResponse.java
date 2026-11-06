package com.devflow.userservice.dto;

import com.devflow.userservice.entity.User;
import com.devflow.userservice.entity.Role;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String fullName,
        Role role,
        boolean enabled,
        Instant created_at
) {
    // A static factory method, converts an entity intp a DTO
    public static UserResponse from(User user){
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
