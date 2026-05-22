package com.devflow.learning.controller;

import com.devflow.common.dto.ApiResponse;
import com.devflow.learning.dto.QuizGenerateRequest;
import com.devflow.learning.dto.QuizResultDto;
import com.devflow.learning.dto.QuizSubmitRequest;
import com.devflow.learning.model.Quiz;
import com.devflow.learning.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Slf4j
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Quiz>> generateQuiz(
            @Valid @RequestBody QuizGenerateRequest request,
            @RequestHeader("X-User-Email") String userEmail) {

        Quiz quiz = quizService.generateQuiz(request, userEmail);
        return ResponseEntity.ok(
                ApiResponse.success("Quiz generated", quiz));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Quiz>> getQuiz(
            @PathVariable String id) {

        return ResponseEntity.ok(ApiResponse.success(
                "Quiz retrieved", quizService.getQuizById(id)));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<ApiResponse<List<Quiz>>> getCourseQuizzes(
            @PathVariable String courseId) {

        return ResponseEntity.ok(ApiResponse.success(
                "Quizzes retrieved",
                quizService.getQuizzesByCourse(courseId)));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<QuizResultDto>> submitQuiz(
            @PathVariable String id,
            @RequestBody QuizSubmitRequest request,
            @RequestHeader("X-User-Email") String userEmail) {

        QuizResultDto result = quizService.submitQuiz(
                id, request, userEmail);
        return ResponseEntity.ok(
                ApiResponse.success("Quiz submitted", result));
    }
}