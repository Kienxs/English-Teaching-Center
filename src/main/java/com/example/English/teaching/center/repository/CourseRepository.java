package com.example.English.teaching.center.repository;

import com.example.English.teaching.center.dto.report.MonthlyRevenueResponse;
import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.StudentCourse;
import com.example.English.teaching.center.entity.Course.Category;
import com.example.English.teaching.center.entity.Course.Status;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CourseRepository extends JpaRepository<Course, UUID> {

// ================= PROCESS DISPLAY FOR STUDENT =================
    Page<Course> findByStatus(Status status, Pageable pageable);

    List<Course> findByTeacherIdAndStatus(UUID teacherId, Status status);

    List<Course> findByCategory(Category category);

    @Query("SELECT sc.course FROM StudentCourse sc WHERE sc.student.id = :studentId AND sc.status = :status")
    List<Course> findCoursesByStudentIdAndStatus(
        @Param("studentId") UUID studentId,
        @Param("status") StudentCourse.Status status
    );

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.lessons WHERE c.id = :courseId")
    Optional<Course> findByIdWithLessons(@Param("courseId") UUID courseId);

    @Modifying
    @Transactional
    @Query("UPDATE Course c SET c.viewCount = c.viewCount + 1 WHERE c.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    @Query("SELECT c FROM Course c WHERE c.status = 'APPROVED' " +
           "AND (:category IS NULL OR c.category = :category) " +
           "AND (:mode IS NULL OR c.mode = :mode) " +
           "AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Course> findCoursesWithFilters(@Param("category") Course.Category category,
                                        @Param("mode") Course.Mode mode,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

// ================= PROCESS DISPLAY FOR TEACHER =================
    Page<Course> findByTeacherIdOrderByCreatedAtDesc(UUID teacherId, Pageable pageable);
    
    boolean existsBySlugAndIdNot(String slug, UUID id);

    Optional<Course> findBySlug(String slug);

    // 1. Total number of views for all of the teacher's courses
    @Query("SELECT SUM(c.viewCount) FROM Course c WHERE c.teacher.id = :teacherId")
    Long sumViewByTeacherId(@Param("teacherId") UUID teacherId);

    // 2. Count the number of courses
    Long countByTeacherId(UUID teacherId);

    // 3. Get the Top 5 best-selling courses (Đã chuyển sang Native Query để dùng được LIMIT và Map)
    @Query(value = "SELECT c.name as name, COUNT(sc.id) as sold, c.view_count as views, c.fee as fee " + 
                   "FROM courses c JOIN student_courses sc ON c.id = sc.course_id " + 
                   "WHERE c.teacher_id = :teacherId " + 
                   "GROUP BY c.id " + 
                   "ORDER BY sold DESC LIMIT 5", nativeQuery = true)
    List<Map<String, Object>> findTopSellingCourses(@Param("teacherId") UUID teacherId);

// ================= PROCESS DISPLAY FOR ADMIN =================
    @Query("SELECT c FROM Course c WHERE c.status = 'PENDING'")
    Page<Course> findPendingCourses(Pageable pageable);

    @Query(value = """
            SELECT 
                  DATE_FORMAT(sc.enrolled_at, '%y-%m') AS revenueMonth,
                  COUNT(sc.id) AS totalEnrollments,
                  COALESCE(SUM(c.fee), 0) AS totalRevenue
            FROM student_courses sc
            JOIN courses c ON sc.course_id = c.id
            WHERE YEAR(sc.enrolled_at) = :year
            GROUP BY DATE_FORMAT(sc.enrolled_at, '%y-%m')
            ORDER BY revenueMonth ASC
            """, nativeQuery = true)
    List<MonthlyRevenueResponse> getRevenueByYear(@Param("year") int year);
}