package com.example.English.teaching.center.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.English.teaching.center.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
}
