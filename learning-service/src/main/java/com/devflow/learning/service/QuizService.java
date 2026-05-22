package com.devflow.learning.service;

import com.devflow.common.enums.ErrorCode;
import com.devflow.common.exception.BaseException;
import com.devflow.learning.config.AiServiceClient;
import com.devflow.learning.dto.QuizGenerateRequest;
import com.devflow.learning.dto.QuizResultDto;
import com.devflow.learning.dto.QuizSubmitRequest;
import com.devflow.learning.model.Course;
import com.devflow.learning.model.Quiz;
import com.devflow.learning.model.UserProgress;
import com.devflow.learning.repository.CourseRepository;
import com.devflow.learning.repository.QuizRepository;
import com.devflow.learning.repository.UserProgressRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizService {

    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;
    private final UserProgressRepository progressRepository;
    private final AiServiceClient aiServiceClient;
    private final ObjectMapper objectMapper;

    public Quiz generateQuiz(QuizGenerateRequest request,
                             String userEmail) {
        log.info("Generating quiz for course: {}",
                request.getCourseId());

        Course course = courseRepository
                .findById(request.getCourseId())
                .orElseThrow(() ->
                        new BaseException(ErrorCode.COURSE_NOT_FOUND));

        // Call AI service to generate questions
        String aiResponse = aiServiceClient.generateQuizContent(
                course.getTitle(),
                course.getDescription(),
                request.getTopic(),
                request.getQuestionCount()
        );

        List<Quiz.Question> questions = parseAiQuestions(aiResponse,
                request.getQuestionCount());

        Quiz quiz = Quiz.builder()
                .courseId(request.getCourseId())
                .title("Quiz: " + course.getTitle() +
                        (request.getTopic() != null
                                ? " - " + request.getTopic() : ""))
                .aiGenerated(true)
                .questions(questions)
                .createdAt(LocalDateTime.now())
                .build();

        Quiz saved = quizRepository.save(quiz);
        log.info("Quiz generated with {} questions, id: {}",
                questions.size(), saved.getId());
        return saved;
    }

    public QuizResultDto submitQuiz(String quizId,
                                    QuizSubmitRequest request,
                                    String userEmail) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.RESOURCE_NOT_FOUND));

        List<QuizResultDto.QuestionResult> results = new ArrayList<>();
        int correct = 0;

        for (Quiz.Question question : quiz.getQuestions()) {
            Integer selected = request.getAnswers()
                    .get(question.getId());
            boolean isCorrect = selected != null &&
                    selected.equals(question.getCorrectOptionIndex());

            if (isCorrect) correct++;

            results.add(QuizResultDto.QuestionResult.builder()
                    .questionId(question.getId())
                    .questionText(question.getQuestionText())
                    .selectedOption(selected != null ? selected : -1)
                    .correctOption(question.getCorrectOptionIndex())
                    .correct(isCorrect)
                    .explanation(question.getExplanation())
                    .build());
        }

        int total = quiz.getQuestions().size();
        int score = total > 0 ? (correct * 100) / total : 0;

        // Update user progress with quiz score
        progressRepository
                .findByUserEmailAndCourseId(userEmail, quiz.getCourseId())
                .ifPresent(progress -> {
                    progress.getQuizScores().put(quizId, score);
                    progress.setLastAccessedAt(LocalDateTime.now());
                    progressRepository.save(progress);
                });

        return QuizResultDto.builder()
                .quizId(quizId)
                .score(score)
                .correctAnswers(correct)
                .totalQuestions(total)
                .passed(score >= 70)
                .results(results)
                .build();
    }

    public Quiz getQuizById(String id) {
        return quizRepository.findById(id)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    public List<Quiz> getQuizzesByCourse(String courseId) {
        return quizRepository.findByCourseId(courseId);
    }

    // Parses the JSON response from AI service into Quiz questions
    private List<Quiz.Question> parseAiQuestions(String aiResponse,
                                                 int expectedCount) {
        List<Quiz.Question> questions = new ArrayList<>();

        try {
            if (aiResponse == null) {
                return getFallbackQuestions(expectedCount);
            }

            // AI response is wrapped in ApiResponse
            // Extract the content field
            JsonNode root = objectMapper.readTree(aiResponse);
            String content = root.path("data")
                    .path("content").asText();

            // Remove markdown code blocks if present
            content = content.replaceAll("```json", "")
                    .replaceAll("```", "").trim();

            // Parse the JSON array of questions
            List<Quiz.Question> parsed = objectMapper.readValue(
                    content,
                    new TypeReference<List<Quiz.Question>>() {});

            for (Quiz.Question q : parsed) {
                q.setId(UUID.randomUUID().toString());
                questions.add(q);
            }

        } catch (Exception e) {
            log.error("Failed to parse AI quiz response: {}",
                    e.getMessage());
            return getFallbackQuestions(expectedCount);
        }

        return questions;
    }

    // Fallback questions if AI service fails
    private List<Quiz.Question> getFallbackQuestions(int count) {
        List<Quiz.Question> questions = new ArrayList<>();
        for (int i = 0; i < Math.min(count, 3); i++) {
            questions.add(Quiz.Question.builder()
                    .id(UUID.randomUUID().toString())
                    .questionText("Sample question " + (i + 1))
                    .options(List.of("Option A", "Option B",
                            "Option C", "Option D"))
                    .correctOptionIndex(0)
                    .explanation("This is a fallback question")
                    .build());
        }
        return questions;
    }
}