package com.devflow.ai.service;

import com.devflow.ai.client.ClaudeApiClient;
import com.devflow.ai.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final ClaudeApiClient claudeApiClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${ai.cache.ttl}")
    private long cacheTtl;

    // ═══════════════════════════════════════════════════════
    // CODE REVIEW
    // Reviews code and returns structured feedback
    // ═══════════════════════════════════════════════════════
    public AiResponse reviewCode(CodeReviewRequest request) {
        String cacheKey = "ai:code-review:" +
                request.getCode().hashCode();

        // Check cache first — same code reviewed before?
        AiResponse cached = getCachedResponse(cacheKey);
        if (cached != null) {
            log.info("Returning cached code review");
            return cached;
        }

        long startTime = System.currentTimeMillis();

        // System prompt = tells Claude what role to play
        // and how to structure its response
        String systemPrompt = """
                You are an expert code reviewer with deep knowledge of
                software engineering best practices, design patterns,
                and security vulnerabilities.
                
                When reviewing code, always structure your response as:
                1. SUMMARY: Brief overview of what the code does
                2. ISSUES: List any bugs, security issues, or problems
                3. IMPROVEMENTS: Specific suggestions to improve the code
                4. BEST PRACTICES: Any best practice violations
                5. SCORE: Rate the code quality from 1-10
                
                Be specific, actionable, and educational.
                """;

        // User message = the actual code to review
        String userMessage = String.format("""
                Please review this %s code:
                
```%s
                %s
```
                
                Context: %s
                """,
                request.getLanguage() != null
                        ? request.getLanguage() : "code",
                request.getLanguage() != null
                        ? request.getLanguage() : "",
                request.getCode(),
                request.getContext() != null
                        ? request.getContext() : "No additional context");

        ClaudeResponse claudeResponse = claudeApiClient
                .sendMessage(systemPrompt, userMessage);

        AiResponse response = buildAiResponse(
                claudeResponse,
                System.currentTimeMillis() - startTime,
                false);

        // Cache the response for 1 hour
        cacheResponse(cacheKey, response);

        return response;
    }

    // ═══════════════════════════════════════════════════════
    // TEST GENERATION
    // Generates unit tests for provided code
    // ═══════════════════════════════════════════════════════
    public AiResponse generateTests(TestGenerationRequest request) {
        String cacheKey = "ai:test-gen:" +
                request.getCode().hashCode();

        AiResponse cached = getCachedResponse(cacheKey);
        if (cached != null) return cached;

        long startTime = System.currentTimeMillis();

        String systemPrompt = """
                You are an expert software engineer specializing in
                test-driven development and writing comprehensive unit tests.
                
                Generate thorough unit tests that:
                1. Cover happy path scenarios
                2. Cover edge cases and boundary conditions
                3. Cover error/exception scenarios
                4. Use meaningful test method names
                5. Include comments explaining what each test verifies
                
                Return ONLY the test code, no explanations outside the code.
                """;

        String framework = request.getTestFramework() != null
                ? request.getTestFramework() : "JUnit 5 with Mockito";

        String userMessage = String.format("""
                Generate comprehensive unit tests for this %s code
                using %s:
                
```%s
                %s
```
                """,
                request.getLanguage() != null
                        ? request.getLanguage() : "code",
                framework,
                request.getLanguage() != null
                        ? request.getLanguage() : "",
                request.getCode());

        ClaudeResponse claudeResponse = claudeApiClient
                .sendMessage(systemPrompt, userMessage);

        AiResponse response = buildAiResponse(
                claudeResponse,
                System.currentTimeMillis() - startTime,
                false);

        cacheResponse(cacheKey, response);
        return response;
    }

    // ═══════════════════════════════════════════════════════
    // RESUME PARSER
    // Extracts structured data from resume text
    // ═══════════════════════════════════════════════════════
    public AiResponse parseResume(ResumeParseRequest request) {
        long startTime = System.currentTimeMillis();

        // Resume parsing is NOT cached — each resume is unique
        String systemPrompt = """
                You are an expert HR analyst and technical recruiter
                with deep knowledge of software engineering skills.
                
                Extract and structure information from resumes.
                Always respond in valid JSON format with these fields:
                {
                  "name": "candidate name",
                  "email": "email if found",
                  "yearsOfExperience": number,
                  "skills": {
                    "languages": ["Java", "Python"],
                    "frameworks": ["Spring Boot", "React"],
                    "databases": ["PostgreSQL", "MongoDB"],
                    "cloud": ["AWS", "GCP"],
                    "tools": ["Docker", "Kubernetes"]
                  },
                  "currentRole": "current job title",
                  "education": "highest degree",
                  "highlights": ["key achievement 1", "key achievement 2"],
                  "matchScore": 85
                }
                Return ONLY valid JSON, no other text.
                """;

        String userMessage = "Parse this resume:\n\n" +
                request.getResumeText();

        ClaudeResponse claudeResponse = claudeApiClient
                .sendMessage(systemPrompt, userMessage);

        return buildAiResponse(
                claudeResponse,
                System.currentTimeMillis() - startTime,
                false);
    }

    // ═══════════════════════════════════════════════════════
    // GENERAL AI CHAT
    // ═══════════════════════════════════════════════════════
    public AiResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        String systemPrompt = request.getSystemContext() != null
                ? request.getSystemContext()
                : """
                  You are a helpful AI assistant for DevFlow,
                  a developer tools and learning platform.
                  You help developers with coding questions,
                  career advice, and technical guidance.
                  Be concise, practical, and developer-friendly.
                  """;

        ClaudeResponse claudeResponse = claudeApiClient
                .sendMessage(systemPrompt, request.getMessage());

        return buildAiResponse(
                claudeResponse,
                System.currentTimeMillis() - startTime,
                false);
    }

    // ═══════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════

    private AiResponse buildAiResponse(ClaudeResponse claudeResponse,
                                       long processingTimeMs,
                                       boolean cached) {
        if (claudeResponse == null) {
            return AiResponse.builder()
                    .content("AI service temporarily unavailable")
                    .cached(false)
                    .processingTimeMs(processingTimeMs)
                    .build();
        }

        return AiResponse.builder()
                .content(claudeResponse.getFirstTextContent())
                .model(claudeResponse.getModel())
                .inputTokens(claudeResponse.getUsage() != null
                        ? claudeResponse.getUsage().getInputTokens() : 0)
                .outputTokens(claudeResponse.getUsage() != null
                        ? claudeResponse.getUsage().getOutputTokens() : 0)
                .cached(cached)
                .processingTimeMs(processingTimeMs)
                .build();
    }

    private AiResponse getCachedResponse(String key) {
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached == null) return null;
        try {
            if (cached instanceof AiResponse) return (AiResponse) cached;
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void cacheResponse(String key, AiResponse response) {
        try {
            redisTemplate.opsForValue().set(
                    key, response, cacheTtl, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Failed to cache AI response: {}", e.getMessage());
        }
    }
}