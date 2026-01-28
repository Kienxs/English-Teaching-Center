package com.example.English.teaching.center.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.English.teaching.center.model.CourseComments;

public interface CourseCommentRepository extends JpaRepository<CourseComments, Long>{
    // Page
    Page<CourseComments> findByCourseId(Long courseId, Pageable pageable);
}
