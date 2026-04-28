package com.example.English.teaching.center.controller.common;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.English.teaching.center.dto.content.CourseCommentResponse;
import com.example.English.teaching.center.dto.course.CourseDetailResponse;
import com.example.English.teaching.center.service.course.CourseCommentService;
import com.example.English.teaching.center.service.course.CourseService;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseCommentService courseCommentService;
    private final CourseService courseService;

    @GetMapping
    public ResponseEntity<Page<CourseDetailResponse>> getAllCourses(
                    @RequestParam(required = false) String category,
                    @RequestParam(required = false) String mode,
                    @RequestParam(required = false) String keyword,
                    @RequestParam(required = false, defaultValue = "newest") String sort,
                    @RequestParam(defaultValue = "0") int page,
                    @RequestParam(defaultValue = "9") int size) {

        Page<CourseDetailResponse> coursePage = courseService.getCoursesWithFilters(category, mode, keyword, sort, page, size);    

        return ResponseEntity.ok(coursePage);
    }

    @GetMapping("/comments/{courseId}")
    public ResponseEntity<List<CourseCommentResponse>> loadMoreComments(
            @PathVariable UUID courseId,
            @RequestParam(defaultValue = "0") int page) {

        Page<CourseCommentResponse> commentPage = courseCommentService.getCommentsByCourseId(courseId, page, 5);
        
        return ResponseEntity.ok(commentPage.getContent());
    }
}