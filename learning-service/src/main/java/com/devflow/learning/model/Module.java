package com.devflow.learning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Also an embedded document — stored inside Course
// A module contains multiple lessons
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Module {

    private String id;
    private String title;
    private String description;
    private Integer orderIndex;

    // List of lessons embedded directly in this module
    // In PostgreSQL this would need a separate table + JOIN
    // In MongoDB it's just a nested array — much simpler
    private List<Lesson> lessons;
}