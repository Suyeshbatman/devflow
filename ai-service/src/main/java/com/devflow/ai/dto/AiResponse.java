package com.devflow.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse {

    // The AI generated text response
    private String content;

    // Which Claude model was used
    private String model;

    // How many tokens were used
    // Important for cost tracking
    private int inputTokens;
    private int outputTokens;

    // Was this response from cache?
    // true = no API call made (free)
    // false = fresh API call (costs tokens)
    private boolean cached;

    // How long the AI took to respond (ms)
    private long processingTimeMs;
}