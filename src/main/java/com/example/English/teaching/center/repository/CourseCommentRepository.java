package com.example.English.teaching.center.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.English.teaching.center.entity.CourseComment;

public interface CourseCommentRepository extends JpaRepository<CourseComment, Long>{
    // Page
    Page<CourseComment> findByCourseId(Long courseId, Pageable pageable);
}
