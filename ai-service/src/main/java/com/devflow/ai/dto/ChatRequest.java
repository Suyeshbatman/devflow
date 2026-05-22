package com.devflow.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {

    @NotBlank(message = "Message is required")
    private String message;

    // Optional system context for the conversation
    private String systemContext;
}