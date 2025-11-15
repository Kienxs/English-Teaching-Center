package com.example.English.teaching.center.repository;

import com.example.English.teaching.center.model.Course;
import com.example.English.teaching.center.model.Course.Category;
import com.example.English.teaching.center.model.StudentCourse;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByCategory(Category category);

    @Query("SELECT sc.course FROM StudentCourse sc WHERE sc.student.id = :studentId AND sc.status = :status")
    List<Course> findCoursesByStudentIdAndStatus(
        @Param("studentId") Long studentId,
        @Param("status") StudentCourse.Status status
    );


}
