package com.devflow.userservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.util.Map;

@RestController
public class HelloController {
    @GetMapping("/api/hello")
    public Map<String, Object> hello(){
        return Map.of(
                "message", "Hello from devflow user-service!",
                "service", "user-service",
                "timestamp", Instant.now().toString()
        );
    }
}
