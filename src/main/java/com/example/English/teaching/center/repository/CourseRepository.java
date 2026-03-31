package com.example.English.teaching.center.repository;

import com.example.English.teaching.center.dto.MonthlyRevenueDTO;
import com.example.English.teaching.center.entity.Course;
import com.example.English.teaching.center.entity.StudentCourse;
import com.example.English.teaching.center.entity.Course.Category;
import com.example.English.teaching.center.entity.Course.Status;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CourseRepository extends JpaRepository<Course, Long> {

// Process diaplay for student ----------------------------------------------------------------------------------
    Page<Course> findByStatus(Status status, Pageable pageable);

    List<Course> findByTeacherIdAndStatus(Long teacherId, Status status);

    List<Course> findByCategory(Category category);

    @Query("SELECT sc.course FROM StudentCourse sc WHERE sc.student.id = :studentId AND sc.status = :status")
    List<Course> findCoursesByStudentIdAndStatus(
        @Param("studentId") Long studentId,
        @Param("status") StudentCourse.Status status
    );

    @Query("SELECT c FROM Course c LEFT JOIN FETCH c.lessons WHERE c.id = :courseId")
    Optional<Course> findByIdWithLessons(@Param("courseId") Long courseId);

    @Modifying
    @Transactional
    @Query("UPDATE Course c SET c.viewCount = c.viewCount + 1 WHERE c.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Query("SELECT c FROM Course c WHERE c.status = 'APPROVED' " +
           "AND (:category IS NULL OR c.category = :category) " +
           "AND (:mode IS NULL OR c.mode = :mode) " +
           "AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Course> findCoursesWithFilters(@Param("category") Course.Category category,
                                        @Param("mode") Course.Mode mode,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

// Process display for teacher -----------------------------------------------------------------------------------
    Page<Course> findByTeacherIdOrderByCreatedAtDesc(Long teacherId, Pageable pageable);
    
    boolean existsBySlugAndIdNot(String slug, Long id);

    Optional<Course> findBySlug(String slug);

    //1. Total number of views for all of the teacher's courses
    @Query("SELECT SUM(c.viewCount) FROM Course c WHERE c.teacher.id = :teacherId")
    Long sumViewByTEacherId(Long teacherId);

    // 2. Count the number of courses
    Long countByTeacherId(Long teacherId);

    // 3. Get the Top 5 best-selling courses.
    @Query("SELECT c.name as name, COUNT(sc) as sold, c.viewCount as views, c.fee as fee " + 
       "FROM Course c JOIN StudentCourse sc ON c.id = sc.course.id " + 
       "WHERE c.teacher.id = :teacherId " + 
       "GROUP BY c.id " + 
       "ORDER BY sold DESC LIMIT 5")
    List<Map<String, Object>> findTopSellingCourses(Long teacherId);

// Process display for admin -----------------------------------------------------------------------------------
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
    List<MonthlyRevenueDTO> getRevenueByYear(@Param("year") int year);
}
