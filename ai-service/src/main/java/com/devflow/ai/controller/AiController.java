package com.devflow.ai.controller;

import com.devflow.ai.dto.*;
import com.devflow.ai.service.AiService;
import com.devflow.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiService;

    @PostMapping("/code-review")
    public ResponseEntity<ApiResponse<AiResponse>> reviewCode(
            @Valid @RequestBody CodeReviewRequest request,
            @RequestHeader("X-User-Email") String userEmail) {

        log.info("Code review requested by: {}", userEmail);
        AiResponse response = aiService.reviewCode(request);
        return ResponseEntity.ok(
                ApiResponse.success("Code review complete", response));
    }

    @PostMapping("/generate-tests")
    public ResponseEntity<ApiResponse<AiResponse>> generateTests(
            @Valid @RequestBody TestGenerationRequest request,
            @RequestHeader("X-User-Email") String userEmail) {

        log.info("Test generation requested by: {}", userEmail);
        AiResponse response = aiService.generateTests(request);
        return ResponseEntity.ok(
                ApiResponse.success("Tests generated", response));
    }

    @PostMapping("/parse-resume")
    public ResponseEntity<ApiResponse<AiResponse>> parseResume(
            @Valid @RequestBody ResumeParseRequest request,
            @RequestHeader("X-User-Email") String userEmail) {

        log.info("Resume parse requested by: {}", userEmail);
        AiResponse response = aiService.parseResume(request);
        return ResponseEntity.ok(
                ApiResponse.success("Resume parsed", response));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiResponse>> chat(
            @Valid @RequestBody ChatRequest request,
            @RequestHeader("X-User-Email") String userEmail) {

        log.info("AI chat requested by: {}", userEmail);
        AiResponse response = aiService.chat(request);
        return ResponseEntity.ok(
                ApiResponse.success("Response generated", response));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("AI service is running", "UP"));
    }
}