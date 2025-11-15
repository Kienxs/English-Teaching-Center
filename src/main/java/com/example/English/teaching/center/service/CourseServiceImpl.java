package com.example.English.teaching.center.service; // Hoặc ...service.impl

import com.example.English.teaching.center.model.Course;
import com.example.English.teaching.center.model.StudentCourse;
import com.example.English.teaching.center.repository.CourseRepository; // Bạn cũng sẽ cần cái này
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; // RẤT QUAN TRỌNG

import java.util.List;
import java.util.Optional;

@Service // Đánh dấu đây là một Spring Bean
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseRepository courseRepository; // Tiêm Repository

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Optional<Course> findCourseById(Long id) {
        return courseRepository.findById(id);
    }

    @Override
    public List<Course> getCoursesByStudentAndStatus(Long studentId, StudentCourse.Status status) {
        return courseRepository.findCoursesByStudentIdAndStatus(studentId, status);
    }
}