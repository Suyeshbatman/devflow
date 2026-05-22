package com.devflow.learning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// Tracks a specific user's progress in a specific course
// One document per user per course
// @CompoundIndex = index on BOTH fields together
// Makes "find progress for user X in course Y" fast
@Document(collection = "user_progress")
@CompoundIndex(def = "{'userEmail': 1, 'courseId': 1}",
        unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgress {

    @Id
    private String id;

    private String userEmail;
    private String courseId;

    // Which lessons have been completed
    // List of lesson IDs
    private List<String> completedLessons;

    // Quiz scores — Map<quizId, score>
    // e.g. {"quiz123": 85, "quiz456": 92}
    private Map<String, Integer> quizScores;

    // Overall completion percentage (0-100)
    @Builder.Default
    private Integer progressPercentage = 0;

    private LocalDateTime lastAccessedAt;
    private LocalDateTime enrolledAt;
}