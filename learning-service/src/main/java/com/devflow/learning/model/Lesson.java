package com.devflow.learning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Embedded document — stored INSIDE a Module document
// No @Document annotation = not a top-level collection
// It's a nested object within Course
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    private String id;
    private String title;

    // Lesson content — could be video URL, text, or code
    private String content;
    private LessonType type;

    // Duration in minutes
    private Integer durationMinutes;

    // Order within the module
    private Integer orderIndex;
}