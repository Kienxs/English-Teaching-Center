package com.example.English.teaching.center.controller.common;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.English.teaching.center.dto.CourseCommentDTO;
import com.example.English.teaching.center.dto.CourseDTO;
import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.Course.Status;
import com.example.English.teaching.center.repository.CourseRepository;
import com.example.English.teaching.center.service.course.CourseCommentService;
import com.example.English.teaching.center.mapper.CourseMapper;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository courseRepository;
    private final CourseCommentService courseCommentService;
    private final CourseMapper courseMapper;

    public CourseController(CourseRepository courseRepository,
                            CourseCommentService courseCommentService,
                            CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.courseCommentService = courseCommentService;
        this.courseMapper = courseMapper;
    }   

    // Endpoint để lấy TẤT CẢ khóa học
    @GetMapping
    public ResponseEntity<Page<CourseDTO>> getAllCourses(
                    @RequestParam(required = false) String category,
                    @RequestParam(required = false) String search,
                    @RequestParam(required = false) String status,
                    @RequestParam(defaultValue = "0") int page,
                    @RequestParam(defaultValue = "10") int size){

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Course> resultPage;

        if (status != null) {
            try {
                Status courseStatus = Status.valueOf(status.toUpperCase());
                resultPage = courseRepository.findByStatus(courseStatus, pageable);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        else{
            resultPage = courseRepository.findByStatus(Status.APPROVED, pageable);
        }

        Page<CourseDTO> safePage = resultPage.map(courseMapper::toDTO);
        return ResponseEntity.ok(safePage);
    }

    @GetMapping("/comments/{courseId}")
    public ResponseEntity<List<CourseCommentDTO>> loadMoreComments(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page) {

        Page<CourseCommentDTO> commentPage = courseCommentService.getCommentsByCourseId(courseId, page, 5);
        
        return ResponseEntity.ok(commentPage.getContent());
    }
}
