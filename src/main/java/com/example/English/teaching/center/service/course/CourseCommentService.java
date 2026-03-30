package com.example.English.teaching.center.service.course;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.example.English.teaching.center.dto.CourseCommentDTO;
import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.CourseComment;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.mapper.CourseCommentMapper;
import com.example.English.teaching.center.repository.CourseCommentRepository;
import com.example.English.teaching.center.repository.CourseRepository;
import com.example.English.teaching.center.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseCommentService {
    private final CourseCommentRepository courseCommentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseCommentMapper commentMapper;

    public CourseCommentService(CourseCommentRepository courseCommentRepository,
                                UserRepository userRepository, 
                                CourseRepository courseRepository,
                                CourseCommentMapper commentMapper){
        this.courseCommentRepository = courseCommentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.commentMapper = commentMapper;
    }

    @Transactional(readOnly = true)
    public Page<CourseCommentDTO> getCommentsByCourseId(Long courseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return courseCommentRepository.findByCourseId(courseId, pageable)
                .map(commentMapper::toDTO);
    }

    @Transactional
    public CourseComment saveComment(Long courseId, String userEmail, String text) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseComment courseComment = new CourseComment();
        courseComment.setUser(user);
        courseComment.setCourse(course);
        courseComment.setCommentText(text);
        courseComment.setCreatedAt(LocalDateTime.now());
        
        return courseCommentRepository.save(courseComment);
    }
}

