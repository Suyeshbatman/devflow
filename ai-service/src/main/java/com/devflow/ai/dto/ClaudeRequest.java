package com.devflow.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Matches Claude API request format exactly:
// {
//   "model": "claude-sonnet-4-20250514",
//   "max_tokens": 2000,
//   "messages": [{"role": "user", "content": "..."}]
// }
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaudeRequest {

    private String model;

    @JsonProperty("max_tokens")
    private int maxTokens;

    private List<Message> messages;

    // Optional system prompt
    // Sets the context/persona for Claude
    private String system;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String role;    // "user" or "assistant"
        private String content; // the message text
    }
}