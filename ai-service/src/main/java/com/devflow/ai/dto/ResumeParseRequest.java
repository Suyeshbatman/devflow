package com.devflow.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResumeParseRequest {

    @NotBlank(message = "Resume text is required")
    private String resumeText;
}