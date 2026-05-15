package com.devflow.user.repository;

import com.devflow.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// @Repository = marks this as a Spring-managed data access component
// JpaRepository<User, Long>:
//   User = the entity this repo manages
//   Long = the type of the primary key (our id field)
//
// By extending JpaRepository we get these for FREE — no code needed:
//   save(user)           → INSERT or UPDATE
//   findById(id)         → SELECT WHERE id = ?
//   findAll()            → SELECT * FROM users
//   delete(user)         → DELETE
//   existsById(id)       → SELECT COUNT(*) > 0
//   count()              → SELECT COUNT(*)
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA reads the method name and generates SQL automatically:
    // "findByEmail" → SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // "existsByEmail" → SELECT COUNT(*) > 0 FROM users WHERE email = ?
    // Used during registration to check if email is already taken
    boolean existsByEmail(String email);
}