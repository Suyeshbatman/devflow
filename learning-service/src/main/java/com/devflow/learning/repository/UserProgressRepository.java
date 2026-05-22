package com.devflow.learning.repository;

import com.devflow.learning.model.UserProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository
        extends MongoRepository<UserProgress, String> {

    // Find specific user's progress in specific course
    Optional<UserProgress> findByUserEmailAndCourseId(
            String userEmail, String courseId);

    // All courses a user is enrolled in (progress records)
    List<UserProgress> findByUserEmail(String userEmail);
}