package com.devflow.learning.service;

import com.devflow.common.enums.ErrorCode;
import com.devflow.common.exception.BaseException;
import com.devflow.learning.dto.CourseDto;
import com.devflow.learning.dto.CourseRequest;
import com.devflow.learning.model.Course;
import com.devflow.learning.model.CourseLevel;
import com.devflow.learning.model.UserProgress;
import com.devflow.learning.repository.CourseRepository;
import com.devflow.learning.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserProgressRepository progressRepository;

    public CourseDto createCourse(CourseRequest request,
                                  String instructorEmail) {
        log.info("Creating course: {} by {}",
                request.getTitle(), instructorEmail);

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .instructorEmail(instructorEmail)
                .level(request.getLevel())
                .tags(request.getTags() != null
                        ? request.getTags() : new ArrayList<>())
                .modules(new ArrayList<>())
                .enrolledUsers(new ArrayList<>())
                .published(false)
                .enrolledCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // MongoDB save — no SQL needed
        // Hibernate generates SQL for PostgreSQL
        // MongoDB driver converts Java object to BSON document
        Course saved = courseRepository.save(course);
        log.info("Course created with id: {}", saved.getId());
        return mapToDto(saved);
    }

    public List<CourseDto> getAllCourses() {
        return courseRepository.findByPublishedTrue()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<CourseDto> getCoursesByLevel(CourseLevel level) {
        return courseRepository.findByLevelAndPublishedTrue(level)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public CourseDto getCourseById(String id) {
        return courseRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.COURSE_NOT_FOUND));
    }

    public CourseDto publishCourse(String id,
                                   String instructorEmail) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.COURSE_NOT_FOUND));

        if (!course.getInstructorEmail().equals(instructorEmail)) {
            throw new BaseException(ErrorCode.FORBIDDEN);
        }

        course.setPublished(true);
        course.setUpdatedAt(LocalDateTime.now());
        return mapToDto(courseRepository.save(course));
    }

    public void enrollUser(String courseId, String userEmail) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.COURSE_NOT_FOUND));

        if (!course.isPublished()) {
            throw new BaseException(ErrorCode.COURSE_NOT_FOUND);
        }

        // Check already enrolled
        if (course.getEnrolledUsers().contains(userEmail)) {
            throw new BaseException(ErrorCode.ALREADY_ENROLLED);
        }

        // Add user to enrolled list
        course.getEnrolledUsers().add(userEmail);
        course.setEnrolledCount(course.getEnrolledCount() + 1);
        courseRepository.save(course);

        // Create progress tracking document
        UserProgress progress = UserProgress.builder()
                .userEmail(userEmail)
                .courseId(courseId)
                .completedLessons(new ArrayList<>())
                .quizScores(new HashMap<>())
                .progressPercentage(0)
                .enrolledAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .build();

        progressRepository.save(progress);
        log.info("User {} enrolled in course {}", userEmail, courseId);
    }

    public UserProgress getProgress(String courseId,
                                    String userEmail) {
        return progressRepository
                .findByUserEmailAndCourseId(userEmail, courseId)
                .orElseThrow(() ->
                        new BaseException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    public List<CourseDto> getMyEnrolledCourses(String userEmail) {
        return courseRepository
                .findByEnrolledUsersContaining(userEmail)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public CourseDto mapToDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .instructorEmail(course.getInstructorEmail())
                .level(course.getLevel())
                .tags(course.getTags())
                .modules(course.getModules())
                .published(course.isPublished())
                .enrolledCount(course.getEnrolledCount())
                .createdAt(course.getCreatedAt())
                .build();
    }
}