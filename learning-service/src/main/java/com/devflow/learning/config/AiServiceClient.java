package com.devflow.learning.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

// Calls AI service to generate quiz questions
// Uses WebClient (non-blocking HTTP client)
// AI calls can take 5-10 seconds
@Component
@Slf4j
public class AiServiceClient {

    private final WebClient webClient;

    public AiServiceClient(
            @Value("${ai.service.url}") String aiServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(aiServiceUrl)
                .build();
    }

    // Sends course content to AI service
    // Gets back generated quiz questions as text
    public String generateQuizContent(String courseTitle,
                                      String courseDescription,
                                      String topic,
                                      int questionCount) {
        log.info("Calling AI service to generate {} questions " +
                "for course: {}", questionCount, courseTitle);

        // Build the prompt for AI service
        String prompt = String.format("""
                Generate %d multiple choice quiz questions about '%s'.
                Course: %s
                Description: %s
                
                Return ONLY a JSON array in this exact format:
                [
                  {
                    "questionText": "What is...?",
                    "options": ["Option A", "Option B", "Option C", "Option D"],
                    "correctOptionIndex": 0,
                    "explanation": "Because..."
                  }
                ]
                Return ONLY the JSON array, no other text.
                """,
                questionCount,
                topic != null ? topic : courseTitle,
                courseTitle,
                courseDescription);

        try {
            // Call AI service /chat endpoint
            Map<String, String> requestBody = Map.of(
                    "message", prompt,
                    "systemContext",
                    "You are an expert educator creating quiz questions. " +
                            "Always respond with valid JSON only.");

            String response = webClient.post()
                    .uri("/api/ai/chat")
                    // Gateway normally injects this header
                    // For internal service calls we set it manually
                    .header("X-User-Email", "system@devflow.com")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            log.info("AI service responded for quiz generation");
            return response;

        } catch (Exception e) {
            log.error("Failed to call AI service: {}", e.getMessage());
            return null;
        }
    }
}