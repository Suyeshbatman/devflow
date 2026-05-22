package com.devflow.ai.client;

import com.devflow.ai.dto.ClaudeRequest;
import com.devflow.ai.dto.ClaudeResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

// WebClient = Spring's non-blocking HTTP client
// Unlike RestTemplate (old, blocking), WebClient
// doesn't hold a thread while waiting for response
// Perfect for AI calls that can take 5-10 seconds
@Component
@Slf4j
public class ClaudeApiClient {

    private final WebClient webClient;
    private final String model;
    private final int maxTokens;

    // Constructor builds the WebClient with base URL + auth header
    public ClaudeApiClient(
            @Value("${claude.base-url}") String baseUrl,
            @Value("${claude.api-key}") String apiKey,
            @Value("${claude.model}") String model,
            @Value("${claude.max-tokens}") int maxTokens) {

        this.model = model;
        this.maxTokens = maxTokens;

        // WebClient.builder() = fluent API to configure HTTP client
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                // Every request to Claude API needs these headers:
                // x-api-key: your API key
                // anthropic-version: API version
                // content-type: we're sending JSON
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // Sends a prompt to Claude and returns the response
    // String → ClaudeResponse (blocking for simplicity)
    public ClaudeResponse sendMessage(String systemPrompt,
                                      String userMessage) {
        log.info("Calling Claude API with model: {}", model);

        ClaudeRequest request = ClaudeRequest.builder()
                .model(model)
                .maxTokens(maxTokens)
                .system(systemPrompt)
                .messages(List.of(
                        new ClaudeRequest.Message("user", userMessage)
                ))
                .build();

        // .post() = HTTP POST request
        // .uri() = endpoint path (appended to baseUrl)
        // .bodyValue() = request body
        // .retrieve() = execute the request
        // .bodyToMono() = expect a single response object
        // .timeout() = fail if Claude takes > 30 seconds
        // .block() = wait for response (sync for our REST API)
        ClaudeResponse response = webClient.post()
                .uri("/v1/messages")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ClaudeResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnError(e -> log.error(
                        "Claude API error: {}", e.getMessage()))
                .block();

        log.info("Claude API response received. Tokens used: " +
                        "input={}, output={}",
                response != null && response.getUsage() != null
                        ? response.getUsage().getInputTokens() : 0,
                response != null && response.getUsage() != null
                        ? response.getUsage().getOutputTokens() : 0);

        return response;
    }
}