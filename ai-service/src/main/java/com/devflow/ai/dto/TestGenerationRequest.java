package com.devflow.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestGenerationRequest {

    @NotBlank(message = "Code is required")
    private String code;

    private String language;

    // Which test framework to use
    // e.g. "JUnit 5", "Mockito", "Jest", "pytest"
    private String testFramework;
}