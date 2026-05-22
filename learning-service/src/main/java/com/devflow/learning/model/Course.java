package com.devflow.learning.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

// @Document = marks this as a MongoDB document
// collection = the MongoDB collection name
// (like a table name in SQL)
// MongoDB creates the collection automatically
// if it doesn't exist
@Document(collection = "courses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    // @Id = MongoDB document ID
    // MongoDB generates this as a 24-char hex string
    // e.g. "507f1f77bcf86cd799439011"
    // NOT a Long like in PostgreSQL
    @Id
    private String id;

    // @Indexed = MongoDB creates an index on this field
    // Makes searches by title much faster
    @Indexed
    private String title;

    private String description;

    // Who created this course
    private String instructorEmail;

    // Difficulty level
    private CourseLevel level;

    // Course category tags
    private List<String> tags;

    // All modules with their lessons embedded
    // This entire structure stored in ONE document
    private List<Module> modules;

    // List of enrolled user emails
    // Simple array — no join table needed
    private List<String> enrolledUsers;

    // Is this course published and visible?
    @Builder.Default
    private boolean published = false;

    @Builder.Default
    private Integer enrolledCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}