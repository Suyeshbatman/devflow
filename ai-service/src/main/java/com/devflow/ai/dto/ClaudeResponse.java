package com.devflow.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// Matches Claude API response format:
// {
//   "id": "msg_...",
//   "content": [{"type": "text", "text": "..."}],
//   "usage": {"input_tokens": 100, "output_tokens": 200}
// }
@Data
public class ClaudeResponse {

    private String id;
    private String model;
    private List<ContentBlock> content;
    private Usage usage;

    @Data
    public static class ContentBlock {
        private String type;  // always "text" for our use case
        private String text;  // the actual response text
    }

    @Data
    public static class Usage {
        @JsonProperty("input_tokens")
        private int inputTokens;

        @JsonProperty("output_tokens")
        private int outputTokens;
    }

    // Helper to extract the text from content blocks
    public String getFirstTextContent() {
        if (content == null || content.isEmpty()) return "";
        return content.stream()
                .filter(b -> "text".equals(b.getType()))
                .findFirst()
                .map(ContentBlock::getText)
                .orElse("");
    }
}