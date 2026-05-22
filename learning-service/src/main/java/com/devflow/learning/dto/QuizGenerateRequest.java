package com.devflow.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QuizGenerateRequest {

    @NotBlank(message = "Course ID is required")
    private String courseId;

    // Number of questions to generate
    @Min(value = 3, message = "Minimum 3 questions")
    @Max(value = 20, message = "Maximum 20 questions")
    private int questionCount = 5;

    // Topic to focus on within the course
    private String topic;
}