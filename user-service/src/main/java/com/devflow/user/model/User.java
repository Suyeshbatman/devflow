package com.devflow.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

// @Entity     = tells JPA/Hibernate: map this class to a database table
// @Table      = specifies the exact table name in PostgreSQL
// @Data       = Lombok: generates getters, setters, toString, equals, hashCode
// @Builder    = enables User.builder().email("x").build() pattern
// @NoArgsConstructor / @AllArgsConstructor = Lombok constructors

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    // implements UserDetails = Spring Security interface
    // Spring Security calls getUsername(), getPassword(),
    // getAuthorities() on this object during authentication

    // @Id         = this is the primary key
    // @GeneratedValue = database auto-increments this (1, 2, 3...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column(unique=true) = PostgreSQL enforces no duplicate emails
    // @Email = validates format (must contain @)
    // @NotBlank = cannot be null or empty string
    @Column(unique = true, nullable = false)
    @Email
    @NotBlank
    private String email;

    @Column(nullable = false)
    @NotBlank
    private String firstName;

    @Column(nullable = false)
    @NotBlank
    private String lastName;

    // Stored as BCrypt hash, never plain text
    // e.g. "$2a$10$N9qo8uLOickgx2ZMRZo..." not "password123"
    @Column(nullable = false)
    private String password;

    // @Enumerated = stores Role as string ("ADMIN", "DEVELOPER")
    // not as integer (0, 1, 2) — more readable in DB
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Is this account active? false = soft-deleted or banned
    @Builder.Default
    private boolean enabled = true;

    // @Column(updatable=false) = set once on creation, never changed
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // ── JPA Lifecycle Hooks ──────────────────────────────
    // @PrePersist = runs automatically BEFORE INSERT into DB
    // @PreUpdate  = runs automatically BEFORE UPDATE in DB
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Spring Security: UserDetails methods ────────────
    // Spring Security calls these during authentication

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Converts our Role enum to Spring Security's format
        // "ROLE_ADMIN", "ROLE_DEVELOPER", "ROLE_VIEWER"
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        // We use email as the username (not a display name)
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}