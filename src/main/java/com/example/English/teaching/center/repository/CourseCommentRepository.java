package com.example.English.teaching.center.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.English.teaching.center.entity.CourseComment;

public interface CourseCommentRepository extends JpaRepository<CourseComment, UUID> {
    Page<CourseComment> findByCourseId(UUID courseId, Pageable pageable);
}