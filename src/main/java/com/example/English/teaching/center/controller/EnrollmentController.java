package com.example.English.teaching.center.controller;

import com.example.English.teaching.center.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/enroll")
    public ResponseEntity<?> enrollStudent(@RequestBody Map<String, Long> payload, Authentication auth) {
        // Kiểm tra đăng nhập
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để mua khóa học.");
        }

        Long courseId = payload.get("courseId");
        if (courseId == null) {
            return ResponseEntity.badRequest().body("ID khóa học không hợp lệ.");
        }

        try {
            // Gọi nghiệp vụ đăng ký từ Service
            enrollmentService.enrollCourse(auth.getName(), courseId);
            return ResponseEntity.ok(Map.of("message", "Thanh toán và đăng ký thành công!"));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            // Bao gồm cả lỗi không đủ tiền và lỗi tìm kiếm dữ liệu
            return ResponseEntity.status(402).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Đã xảy ra lỗi hệ thống khi xử lý thanh toán.");
        }
    }
}