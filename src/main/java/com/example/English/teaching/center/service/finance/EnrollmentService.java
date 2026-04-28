package com.example.English.teaching.center.service.finance;

import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.StudentCourse;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;

    @Transactional(rollbackFor = Exception.class)
    public void enrollCourse(String email, UUID courseId) {
        // 1. Tìm thông tin người dùng và khóa học
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại."));

        // 2. Kiểm tra nếu đã mua khóa học trước đó
        if (studentCourseRepository.existsByStudentIdAndCourseId(student.getId(), courseId))
            throw new IllegalStateException("Bạn đã sở hữu khóa học này rồi.");

        // 3. Thực hiện thanh toán thông qua WalletService 
        BigDecimal courseFee = course.getFee() == null ? BigDecimal.ZERO : course.getFee();
        if (courseFee.compareTo(BigDecimal.ZERO) > 0) 
            walletService.payment(email, courseFee, "Mua khóa học: " + course.getName());

        // 4. Cấp quyền truy cập khóa học (Enrollment)
        StudentCourse enrollment = new StudentCourse();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus(StudentCourse.Status.ENROLLED);
        
        // Xử lý thời hạn truy cập nếu có
        if (course.getAccessPeriodDays() != null && course.getAccessPeriodDays() > 0) 
            enrollment.setExpiresAt(LocalDateTime.now().plusDays(course.getAccessPeriodDays()));
        
        try {
            studentCourseRepository.save(enrollment);
        } catch (DataIntegrityViolationException e) {
            // Bắt lỗi khi 2 request cùng cố gắng insert khóa học cho 1 user
            throw new IllegalStateException("Giao dịch đang được xử lý, bạn đã đăng ký khóa học này rồi!");
        }
    }
}