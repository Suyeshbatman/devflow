package com.devflow.learning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

// Separate top-level MongoDB collection for quizzes
// Not embedded in Course because:
// 1. Quizzes can be large (many questions)
// 2. We query quizzes independently
// 3. User submissions need to reference quizzes
@Document(collection = "quizzes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {

    @Id
    private String id;

    // Which course this quiz belongs to
    private String courseId;

    private String title;

    // AI-generated or manually created
    @Builder.Default
    private boolean aiGenerated = false;

    private List<Question> questions;

    private LocalDateTime createdAt;

    // Nested class — embedded inside Quiz document
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Question {
        private String id;
        private String questionText;
        private List<String> options;      // ["Option A", "Option B", ...]
        private Integer correctOptionIndex; // 0, 1, 2, or 3
        private String explanation;         // why the answer is correct
    }
}