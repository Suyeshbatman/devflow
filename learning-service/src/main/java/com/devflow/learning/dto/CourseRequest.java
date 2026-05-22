package com.devflow.learning.dto;

import com.devflow.learning.model.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CourseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Level is required")
    private CourseLevel level;

    private List<String> tags;
}