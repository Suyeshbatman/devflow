package com.devflow.userservice.service;

// Our own types: RECORDS, CLASSES, the ENUM, an EXCEPTION and the repository INTERFACE.
import com.devflow.userservice.config.JwtProperties;
import com.devflow.userservice.dto.LoginRequest;
import com.devflow.userservice.dto.LoginResponse;
import com.devflow.userservice.entity.Role;
import com.devflow.userservice.entity.User;
import com.devflow.userservice.exception.InvalidCredentialsException;
import com.devflow.userservice.repository.UserRepository;
// @BeforeEach is an ANNOTATION: run this METHOD before EVERY test (fresh setup each time).
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
// @ExtendWith is an ANNOTATION: plug an extension into JUnit.
import org.junit.jupiter.api.extension.ExtendWith;
// @Mock is an ANNOTATION: "create a fake object for this FIELD".
import org.mockito.Mock;
// MockitoExtension is a CLASS (a JUnit extension) that creates the @Mock objects.
import org.mockito.internal.verification.Only;
import org.mockito.junit.jupiter.MockitoExtension;
// PasswordEncoder INTERFACE (we'll fake it).
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;   // CLASS: a length of time
import java.util.Optional;   // CLASS: a "maybe" box

// "import static" brings in STATIC METHODS, so we can write assertThat(...)
//   instead of Assertions.assertThat(...).
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


// Let Mockito create the @Mock fields before each test.
//   There's NO Spring here: no @SpringBootTest, no database. That's why this runs in milliseconds.
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;
    private AuthService authService;
    private User user;

    @BeforeEach
    void setUp(){
        // [LOCAL VARIABLE] + [CONSTRUCTOR CALL] A real JwtProperties RECORD. Only expiration matters here.
        //   [STATIC METHOD CALL] Duration.ofHours(1) creates a Duration OBJECT of one hour
        JwtProperties jwtProperties = new JwtProperties("not-used-here", Duration.ofHours(1), "test-issuer");
        // [CONSTRUCTOR CALL] We create AuthService OURSELVES and pass in the fakes.
        //   This is a big payoff of constructor injection: tests can hand in any dependencies they like.
        authService = new AuthService(userRepository, passwordEncoder, tokenService, jwtProperties);
        // [CONSTRUCTOR CALL] A User entity OBJECT in memory. "stored-hash" stands in for a BCrypt hash.
        user = new User("neesha@example.com", "stored-hash", "Neesha Dev", Role.DEVELOPER);
    }
    // A test. The method name describes the behavior being checked.
    @Test
    void loginWithCorrectCredentialsReturnsTokenAndRecordsLogin(){
        // ---- Arrange: program the fakes ("stubbing") ----
        when(userRepository.findByEmail("neesha@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SuperSecret123","stored-hash")).thenReturn(true);
        when(tokenService.generateToken(user)).thenReturn("fake.jwt.token");
        // ---- Act: call the ONE method under test ----
        LoginResponse response = authService.login(new LoginRequest("  Neesha@example.com", "SuperSecret123"));
        // ---- Assert: check the results ----
        assertThat(response.accessToken()).isEqualTo("fake.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600);
        assertThat(user.getLastLoginAt()).isNotNull();
    }
    @Test
    // Wrong password → exception, and NO token is ever created.
    void loginWithWrongPasswordThrowsAndIssuesNoToken() {
        // Arrange
        when(userRepository.findByEmail("neesha@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "stored-hash")).thenReturn(false);

        // Act + Assert together:
        // [STATIC METHOD CALL] assertThatThrownBy(...) takes a [LAMBDA] () -> ..., runs it, and expects an exception.
        assertThatThrownBy(() -> authService.login(new LoginRequest("neesha@example.com", "WrongPassword")))
                // [METHOD CALL] .isInstanceOf(...): the exception must be this CLASS (or a subclass).
                .isInstanceOf(InvalidCredentialsException.class);

        // [STATIC METHOD CALL] verify(mock, never()): check that generateToken was NEVER called, with any() argument.
        //   Mocks remember every call made to them, so we can check interactions, not only return values.
        verify(tokenService, never()).generateToken(any());
        // The failed login must NOT be recorded as a successful one.
        assertThat(user.getLastLoginAt()).isNull();
    }

    @Test
        // Unknown email → the same exception (no hint that the email doesn't exist).
    void loginWithUnknownEmailThrows() {
        // [STATIC METHOD CALL] Optional.empty(): the repository finds nobody.
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", "whatever123")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
        // Correct password, but the account is disabled → rejected.
    void loginWithDisabledAccountThrows() {
        // [METHOD CALL] setEnabled(false) on the user OBJECT.
        user.setEnabled(false);
        when(userRepository.findByEmail("neesha@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SuperSecret123", "stored-hash")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(new LoginRequest("neesha@example.com", "SuperSecret123")))
                .isInstanceOf(InvalidCredentialsException.class);
        verify(tokenService, never()).generateToken(any());
    }
}
