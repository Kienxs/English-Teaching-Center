package com.example.English.teaching.center.controller.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.English.teaching.center.service.finance.EnrollmentService;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/enroll")
    public ResponseEntity<?> enrollStudent(@RequestBody Map<String, String> payload, 
                                            Authentication auth) {
        // Kiểm tra đăng nhập
        if (auth == null || !auth.isAuthenticated()) 
            return ResponseEntity.status(401).body("Vui lòng đăng nhập để mua khóa học.");

        String courseIdStr = payload.get("courseId");
        if (courseIdStr == null || courseIdStr.trim().isEmpty()) 
            return ResponseEntity.badRequest().body("ID khóa học không hợp lệ.");

        UUID courseId;
        try {
            // 2. Tự parse UUID an toàn
            courseId = UUID.fromString(courseIdStr); 
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Định dạng ID khóa học không đúng.");
        }
        
        try {
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