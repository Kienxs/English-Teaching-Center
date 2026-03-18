package com.example.English.teaching.center.controller;

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

import com.example.English.teaching.center.dto.CommentDTO;
import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.CourseComments;
import com.example.English.teaching.center.entity.Course.Status;
import com.example.English.teaching.center.repository.CourseRepository;
import com.example.English.teaching.center.service.CourseCommentService;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository courseRepository;
    private final CourseCommentService courseCommentService;

    public CourseController(CourseRepository courseRepository,
                            CourseCommentService courseCommentService) {
        this.courseRepository = courseRepository;
        this.courseCommentService = courseCommentService;
    }   

    // Endpoint để lấy TẤT CẢ khóa học
    @GetMapping
    public ResponseEntity<Page<Course>> getAllCourses(
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

        return ResponseEntity.ok(resultPage);
    }

    @GetMapping("/comments/{courseId}")
    public ResponseEntity<List<CommentDTO>> loadMoreComments(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page) {
        
        // 1. Lấy dữ liệu phân trang từ Service (5 item/trang)
        Page<CourseComments> commentPage = courseCommentService.getCommentsByCourseId(courseId, page, 5);
        
        // 2. Định dạng ngày tháng cho đẹp (thay vì toString dài ngoằng)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // 3. Convert Entity -> DTO
        List<CommentDTO> dtos = commentPage.getContent().stream().map(cmt -> {
            CommentDTO dto = new CommentDTO();
            
            // Null check an toàn
            String userName = (cmt.getUser() != null) ? cmt.getUser().getFullName() : "Người dùng ẩn danh";
            String avatarUrl = (cmt.getUser() != null) ? cmt.getUser().getAvatarUrl() : null;

            dto.setUserName(userName);
            dto.setUserAvatar(avatarUrl);
            dto.setContent(cmt.getCommentText());
            
            // Format thời gian
            if (cmt.getCreatedAt() != null) {
                dto.setTimeAgo(cmt.getCreatedAt().format(formatter));
            } else {
                dto.setTimeAgo("");
            }
            
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
