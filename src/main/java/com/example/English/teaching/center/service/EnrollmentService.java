package com.example.English.teaching.center.service;

import com.example.English.teaching.center.model.*;
import com.example.English.teaching.center.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class EnrollmentService {

    private final StudentCourseRepository studentCourseRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public EnrollmentService(StudentCourseRepository studentCourseRepository,
                                 CourseRepository courseRepository,
                                 UserRepository userRepository,
                                 TransactionRepository transactionRepository) {
        this.studentCourseRepository = studentCourseRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public void enrollCourse(String email, Long courseId) {
        // 1. Tìm thông tin người dùng và khóa học
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));
        
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Khóa học không tồn tại."));

        // 2. Kiểm tra nếu đã mua khóa học trước đó
        if (studentCourseRepository.existsByStudentIdAndCourseId(student.getId(), courseId)) {
            throw new IllegalStateException("Bạn đã sở hữu khóa học này rồi.");
        }

        // 3. Kiểm tra số dư tài khoản
        BigDecimal currentBalance = student.getBalance() == null ? BigDecimal.ZERO : student.getBalance();
        BigDecimal courseFee = course.getFee() == null ? BigDecimal.ZERO : course.getFee();

        if (currentBalance.compareTo(courseFee) < 0) {
            throw new RuntimeException("Số dư không đủ để thực hiện thanh toán.");
        }

        // 4. Thực hiện trừ tiền
        BigDecimal newBalance = currentBalance.subtract(courseFee);
        student.setBalance(newBalance);
        userRepository.save(student);

        // 5. Ghi nhật ký giao dịch (PAYMENT)
        Transaction tx = new Transaction();
        tx.setUser(student);
        tx.setAmount(courseFee.negate()); // Số âm biểu thị tiền chi ra
        tx.setBalanceAfter(newBalance);
        tx.setType(Transaction.TransactionType.PAYMENT);
        tx.setDescription("Mua khóa học: " + course.getName());
        transactionRepository.save(tx);

        // 6. Cấp quyền truy cập khóa học (Enrollment)
        StudentCourse enrollment = new StudentCourse();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setStatus(StudentCourse.Status.ENROLLED);
        
        // Xử lý thời hạn truy cập nếu có
        if (course.getAccessPeriodDays() != null && course.getAccessPeriodDays() > 0) {
            enrollment.setExpiresAt(LocalDateTime.now().plusDays(course.getAccessPeriodDays()));
        }
        
        studentCourseRepository.save(enrollment);
    }
}