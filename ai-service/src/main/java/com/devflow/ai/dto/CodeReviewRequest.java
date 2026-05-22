package com.devflow.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CodeReviewRequest {

    @NotBlank(message = "Code is required")
    private String code;

    // Programming language helps Claude give
    // language-specific advice
    // e.g. "java", "python", "javascript"
    private String language;

    // Optional context about what the code does
    private String context;
}