package com.devflow.learning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDto {

    private String quizId;
    private int score;           // percentage 0-100
    private int correctAnswers;
    private int totalQuestions;
    private boolean passed;      // score >= 70 = passed
    private List<QuestionResult> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResult {
        private String questionId;
        private String questionText;
        private int selectedOption;
        private int correctOption;
        private boolean correct;
        private String explanation;
    }
}