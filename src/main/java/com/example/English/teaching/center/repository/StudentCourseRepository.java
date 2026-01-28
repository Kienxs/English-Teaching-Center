package com.example.English.teaching.center.repository;

import com.example.English.teaching.center.model.StudentCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Long> {

    // 1. Kiểm tra xem học viên đã đăng ký khóa học này chưa
    // Hàm này được dùng trong EnrollmentController để chặn mua trùng
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    // 2. Lấy danh sách các khóa học mà một học viên đã đăng ký
    List<StudentCourse> findByStudentId(Long studentId);

    // 3. Lấy bản ghi đăng ký cụ thể (ví dụ để hủy khóa học hoặc gia hạn)
    StudentCourse findByStudentIdAndCourseId(Long studentId, Long courseId);

// Get the teacher's control panel ----------------------------------------------------------------
    // 1. Total revenue 
    @Query("SELECT SUM(c.fee) FROM StudentCourse sc JOIN sc.course c "+
        " WHERE c.teacher.id = :teacherId")
    BigDecimal calculateTotalRevenueByTeacherId(Long teacherId);

    // 2. Total number of students who have registered
    @Query("SELECT COUNT(sc) FROM StudentCourse sc"+
    " JOIN sc.course c WHERE c.teacher.id = :teacherId")
    Long countTotalStudentByTeacher(Long teacherId);

    // 3. Select the 5 most recently registered students 
    @Query("SELECT sc FROM StudentCourse sc "+
        "JOIN sc.course c WHERE c.teacher.id = :teacherId "+
        "ORDER BY sc.enrolledAt DESC LIMIT 5")
    List<StudentCourse> findRecentErollments(Long teacherId);

    // 4. Sale volume statistics by category(For pie chart)
    @Query("SELECT c.category, COUNT(sc) FROM StudentCourse sc" +
        " JOIN sc.course c WHERE c.teacher.id = :teacherId"+
        " GROUP BY c.category")
    List<Object[]> countSalesByCategory(Long teacherId);

    // 1. Query cho 24H: Nhóm theo GIỜ (Hour)
    @Query(value = "SELECT HOUR(sc.enrolled_at) as timePoint, SUM(c.fee) as total " +
                   "FROM student_courses sc " +
                   "JOIN courses c ON sc.course_id = c.id " +
                   "WHERE c.teacher_id = :teacherId " +
                   "AND sc.enrolled_at >= :startDate " +
                   "GROUP BY HOUR(sc.enrolled_at) " +
                   "ORDER BY timePoint ASC", nativeQuery = true)
    List<Object[]> getRevenueByHour(@Param("teacherId") Long teacherId, 
                                    @Param("startDate") LocalDateTime startDate);

    // 2. Query cho Tuần/Tháng: Nhóm theo NGÀY (Date) - (Giữ nguyên logic cũ nhưng viết lại native cho đồng bộ)
    @Query(value = "SELECT DATE(sc.enrolled_at) as timePoint, SUM(c.fee) as total " +
                   "FROM student_courses sc " +
                   "JOIN courses c ON sc.course_id = c.id " +
                   "WHERE c.teacher_id = :teacherId " +
                   "AND sc.enrolled_at >= :startDate " +
                   "GROUP BY DATE(sc.enrolled_at) " +
                   "ORDER BY timePoint ASC", nativeQuery = true)
    List<Object[]> getRevenueByDay(@Param("teacherId") Long teacherId, 
                                   @Param("startDate") LocalDateTime startDate);

    // 3. Query cho Năm: Nhóm theo THÁNG (Month)
    @Query(value = "SELECT DATE_FORMAT(sc.enrolled_at, '%Y-%m') as timePoint, SUM(c.fee) as total " +
                   "FROM student_courses sc " +
                   "JOIN courses c ON sc.course_id = c.id " +
                   "WHERE c.teacher_id = :teacherId " +
                   "AND sc.enrolled_at >= :startDate " +
                   "GROUP BY DATE_FORMAT(sc.enrolled_at, '%Y-%m') " +
                   "ORDER BY timePoint ASC", nativeQuery = true)
    List<Object[]> getRevenueByMonth(@Param("teacherId") Long teacherId, 
                                     @Param("startDate") LocalDateTime startDate);
}