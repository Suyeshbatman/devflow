package com.devflow.learning.dto;

import lombok.Data;

import java.util.Map;

@Data
public class QuizSubmitRequest {

    // Map<questionId, selectedOptionIndex>
    // e.g. {"q1": 2, "q2": 0, "q3": 1}
    private Map<String, Integer> answers;
}