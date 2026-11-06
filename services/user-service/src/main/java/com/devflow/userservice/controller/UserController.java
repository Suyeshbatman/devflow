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
        URI location = URI.create("/api/users" + created.id());
        return ResponseEntity.created(location).body(created);
    }
    // GET /api/users/{id}
    @GetMapping("/{id}")
    // @PathVariable: take {id} from the URL and convert it to a UUID.
    // If the text isn't a valid UUID (e.g. "abc"), Spring automatically answers 400.
    // Returning an object directly (not ResponseEntity) means status 200 OK.
    public UserResponse getById(@PathVariable UUID id){
        return userService.getById(id);
    }
}
