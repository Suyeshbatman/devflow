package com.devflow.userservice.controller;

import com.devflow.userservice.dto.RegisterRequest;
import com.devflow.userservice.dto.UserResponse;
import com.devflow.userservice.service.UserService;
// @Valid: "validate this object using its annotations before calling my method".
import jakarta.validation.Valid;
// ResponseEntity: lets us control the full HTTP response: status code, headers and body.
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Injects the currently logged-in user's information into a controller method parameter.
import org.springframework.security.core.annotation.AuthenticationPrincipal;
// The verified, decoded token.
import org.springframework.security.oauth2.jwt.Jwt;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /api/users/register
    @PostMapping("/register")
    // @RequestBody: convert the JSON body of the request into a RegisterRequest object.
    // @Valid: check its validation rules first. If any fail, this method never runs,
    //         and our handler returns 400 instead.
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        UserResponse created = userService.register(request);
        URI location = URI.create("/api/users/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    // GET /api/users/me → the profile of whoever owns the token.
    // There's no id in the URL: we read it from the token, so users can only ever see THEMSELVES this way.
    @GetMapping("/me")
    // @AuthenticationPrincipal Jwt jwt: Spring hands us the token it already verified in the filter chain.
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        // getSubject() returns the "sub" claim, which is the user id we put there at login.
        // UUID.fromString(...) is a static method that converts the text back into a UUID.
        UUID userId = UUID.fromString(jwt.getSubject());
        return userService.getById(userId);
    }

    // GET /api/users/{id} now ADMIN-only
    @GetMapping("/{id}")
    // @PathVariable: take {id} from the URL and convert it to a UUID.
    // If the text isn't a valid UUID (e.g. "abc"), Spring automatically answers 400.
    // Returning an object directly (not ResponseEntity) means status 200 OK.
    public UserResponse getById(@PathVariable UUID id){
        return userService.getById(id);
    }
}
