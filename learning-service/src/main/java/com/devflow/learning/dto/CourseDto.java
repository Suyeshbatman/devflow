package com.devflow.learning.dto;

import com.devflow.learning.model.CourseLevel;
import com.devflow.learning.model.Module;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private String id;
    private String title;
    private String description;
    private String instructorEmail;
    private CourseLevel level;
    private List<String> tags;
    private List<Module> modules;
    private boolean published;
    private Integer enrolledCount;
    private LocalDateTime createdAt;
}