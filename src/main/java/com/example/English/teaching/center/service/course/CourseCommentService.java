package com.example.English.teaching.center.service.course;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.example.English.teaching.center.dto.content.CourseCommentRequest;
import com.example.English.teaching.center.dto.content.CourseCommentResponse;
import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.CourseComment;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.mapper.CourseCommentMapper;
import com.example.English.teaching.center.repository.CourseCommentRepository;
import com.example.English.teaching.center.repository.CourseRepository;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.service.infra.RateLimitingService;

import org.springframework.transaction.annotation.Transactional;
import io.github.bucket4j.Bucket;

@Service
public class CourseCommentService {
    private final CourseCommentRepository courseCommentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseCommentMapper commentMapper;
    private final RateLimitingService rateLimitingService;

    public CourseCommentService(CourseCommentRepository courseCommentRepository,
                                UserRepository userRepository, 
                                CourseRepository courseRepository,
                                CourseCommentMapper commentMapper,
                                RateLimitingService rateLimitingService){
        this.courseCommentRepository = courseCommentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.commentMapper = commentMapper;
        this.rateLimitingService = rateLimitingService;
    }

    @Transactional(readOnly = true)
    public Page<CourseCommentResponse> getCommentsByCourseId(Long courseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return courseCommentRepository.findByCourseId(courseId, pageable)
                .map(commentMapper::toDTO);
    }

    @Transactional
    public CourseComment saveComment(CourseCommentRequest dto, String email) {
        String limitKey = "COMMENT_" + email;

        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 5, 1);
        if(!bucket.tryConsume(1))
            throw new RateLimitException("Bạn thao tác quá nhanh! Vui lòng đợi 1 phút.");

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(dto.getCourseId())
            .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseComment courseComment = new CourseComment();
        courseComment.setUser(user);
        courseComment.setCourse(course);
        courseComment.setCommentText(dto.getText());
        courseComment.setCreatedAt(LocalDateTime.now());
        
        return courseCommentRepository.save(courseComment);
    }
}