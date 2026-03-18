package com.example.English.teaching.center.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.CourseComments;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.repository.CourseCommentRepository;
import com.example.English.teaching.center.repository.CourseRepository;
import com.example.English.teaching.center.repository.UserRepository;

@Service
public class CourseCommentService {
    private final CourseCommentRepository courseCommentRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public CourseCommentService(CourseCommentRepository courseCommentRepository,
                                UserRepository userRepository, 
                                CourseRepository courseRepository){
        this.courseCommentRepository = courseCommentRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    public Page<CourseComments> getCommentsByCourseId(Long courseId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return courseCommentRepository.findByCourseId(courseId, pageable);
    }

    public CourseComments saveComment(Long courseId, String userEmail, String text) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findByIdWithLessons(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));

        CourseComments courseComment = new CourseComments();
        courseComment.setUser(user);
        courseComment.setCourse(course);
        courseComment.setCommentText(text);
        courseComment.setCreatedAt(LocalDateTime.now());
        return courseCommentRepository.save(courseComment);
    }
}

