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

import com.cloudinary.http5.api.Response;
import com.example.English.teaching.center.dto.CourseCommentDTO;
import com.example.English.teaching.center.dto.CourseDTO;
import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.Course.Status;
import com.example.English.teaching.center.repository.CourseRepository;
import com.example.English.teaching.center.service.course.CourseCommentService;
import com.example.English.teaching.center.service.course.CourseService;
import com.example.English.teaching.center.mapper.CourseMapper;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseCommentService courseCommentService;
    private final CourseService courseService;

    public CourseController(CourseCommentService courseCommentService, 
                            CourseService courseService) {
        this.courseCommentService = courseCommentService;
        this.courseService = courseService;
    }   

    @GetMapping
    public ResponseEntity<Page<CourseDTO>> getAllCourses(
                    @RequestParam(required = false) String category,
                    @RequestParam(required = false) String mode,
                    @RequestParam(required = false) String keyword,
                    @RequestParam(required = false, defaultValue = "newest") String sort,
                    @RequestParam(defaultValue = "0") int page,
                    @RequestParam(defaultValue = "9") int size) {

        Page<CourseDTO> coursePage = courseService.getCoursesWithFilters(category, mode, keyword, sort, page, size);    

        return ResponseEntity.ok(coursePage);
    }

    @GetMapping("/comments/{courseId}")
    public ResponseEntity<List<CourseCommentDTO>> loadMoreComments(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page) {

        Page<CourseCommentDTO> commentPage = courseCommentService.getCommentsByCourseId(courseId, page, 5);
        
        return ResponseEntity.ok(commentPage.getContent());
    }
}
