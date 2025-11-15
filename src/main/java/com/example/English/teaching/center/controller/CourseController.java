package com.example.English.teaching.center.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.English.teaching.center.model.Course;
import com.example.English.teaching.center.model.StudentCourse;
import com.example.English.teaching.center.model.User;
import com.example.English.teaching.center.repository.CourseRepository;
import com.example.English.teaching.center.service.CourseService;
import com.example.English.teaching.center.service.UserService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.security.Principal;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;
    

    // Endpoint để lấy TẤT CẢ khóa học
    @GetMapping
    public List<Course> getAllCourses(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String search) 
    {
        if (category != null) {
            // (Bạn cần thêm logic để chuyển string "IELTS" thành Enum Category.IELTS)
            // return courseRepository.findByCategory(...);
        }
        if (search != null) {
            // (Bạn cần thêm hàm tìm kiếm theo tên)
            // return courseRepository.findByNameContaining(search);
        }
        // Nếu không có filter, trả về tất cả
        return courseRepository.findAll();
    }

    @GetMapping("/user/my-courses")
    public ResponseEntity<List<Course>> getMyEnrolledCourses(
    Principal principal,
    @RequestParam(name = "status", defaultValue = "ENROLLED")
    String statusString) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = principal.getName();
        User currentUser = userService.findByEmail(email);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Long currentStudentId = currentUser.getId();

        // 2. Chuyển đổi String (ENROLLED, EXPIRED) sang Enum
        StudentCourse.Status status;
        try {
            status = StudentCourse.Status.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Nếu JavaScript gửi bậy, mặc định về ENROLLED
            status = StudentCourse.Status.ENROLLED;
        }

        // 3. Gọi hàm service mới
        List<Course> courses = courseService.getCoursesByStudentAndStatus(currentStudentId, status);
        return ResponseEntity.ok(courses);
    }
}
