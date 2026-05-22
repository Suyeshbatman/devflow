package com.devflow.learning.repository;

import com.devflow.learning.model.Course;
import com.devflow.learning.model.CourseLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// MongoRepository<Course, String>
// Course = document type
// String = ID type (MongoDB uses String IDs not Long)
// Works exactly like JpaRepository
// Same methods: save(), findById(), findAll(), delete()
// Spring generates the implementation automatically
@Repository
public interface CourseRepository
        extends MongoRepository<Course, String> {

    // Spring Data MongoDB reads method names
    // and generates MongoDB queries:
    // findByPublishedTrue →
    //   db.courses.find({ "published": true })
    List<Course> findByPublishedTrue();

    // findByLevelAndPublishedTrue →
    //   db.courses.find({ "level": ?, "published": true })
    List<Course> findByLevelAndPublishedTrue(CourseLevel level);

    // findByTagsContaining →
    //   db.courses.find({ "tags": { $in: [?] } })
    List<Course> findByTagsContaining(String tag);

    // Find courses by instructor
    List<Course> findByInstructorEmail(String email);

    // Find courses a user is enrolled in
    // enrolledUsers is an array in MongoDB
    // $elemMatch finds documents where array contains value
    List<Course> findByEnrolledUsersContaining(String userEmail);
}