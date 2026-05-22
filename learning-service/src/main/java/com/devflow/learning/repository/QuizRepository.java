package com.devflow.learning.repository;

import com.devflow.learning.model.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository
        extends MongoRepository<Quiz, String> {

    List<Quiz> findByCourseId(String courseId);
}