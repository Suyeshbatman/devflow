package com.devflow.learning.controller;

import com.devflow.common.dto.ApiResponse;
import com.devflow.learning.dto.CourseDto;
import com.devflow.learning.dto.CourseRequest;
import com.devflow.learning.model.CourseLevel;
import com.devflow.learning.model.UserProgress;
import com.devflow.learning.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<ApiResponse<CourseDto>> createCourse(
            @Valid @RequestBody CourseRequest request,
            @RequestHeader("X-User-Email") String userEmail) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Course created",
                        courseService.createCourse(request, userEmail)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseDto>>> getCourses(
            @RequestParam(required = false) CourseLevel level) {

        List<CourseDto> courses = level != null
                ? courseService.getCoursesByLevel(level)
                : courseService.getAllCourses();

        return ResponseEntity.ok(
                ApiResponse.success("Courses retrieved", courses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDto>> getCourse(
            @PathVariable String id) {

        return ResponseEntity.ok(ApiResponse.success(
                "Course retrieved", courseService.getCourseById(id)));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<CourseDto>> publishCourse(
            @PathVariable String id,
            @RequestHeader("X-User-Email") String userEmail) {

        return ResponseEntity.ok(ApiResponse.success(
                "Course published",
                courseService.publishCourse(id, userEmail)));
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<ApiResponse<Void>> enroll(
            @PathVariable String id,
            @RequestHeader("X-User-Email") String userEmail) {

        courseService.enrollUser(id, userEmail);
        return ResponseEntity.ok(
                ApiResponse.success("Enrolled successfully"));
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<UserProgress>> getProgress(
            @PathVariable String id,
            @RequestHeader("X-User-Email") String userEmail) {

        return ResponseEntity.ok(ApiResponse.success(
                "Progress retrieved",
                courseService.getProgress(id, userEmail)));
    }

    @GetMapping("/my-courses")
    public ResponseEntity<ApiResponse<List<CourseDto>>> getMyCourses(
            @RequestHeader("X-User-Email") String userEmail) {

        return ResponseEntity.ok(ApiResponse.success(
                "Enrolled courses retrieved",
                courseService.getMyEnrolledCourses(userEmail)));
    }
}