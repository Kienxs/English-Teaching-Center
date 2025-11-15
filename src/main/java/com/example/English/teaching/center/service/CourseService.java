package com.example.English.teaching.center.service;

import com.example.English.teaching.center.model.Course;
import com.example.English.teaching.center.model.StudentCourse;

import java.util.List;
import java.util.Optional;

public interface CourseService {
    // Định nghĩa các phương thức bạn cần
    List<Course> getAllCourses();
    Optional<Course> findCourseById(Long id);
    // ... (ví dụ: createCourse, updateCourse, ...)
    List<Course> getCoursesByStudentAndStatus(Long studentId, StudentCourse.Status status);
}