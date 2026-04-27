package com.example.English.teaching.center.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.English.teaching.center.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @Query("SELECT c FROM Comment c " +
               "WHERE c.post.id = :postId " +
               "AND (:lastCreatedAt IS NULL OR c.createdAt < :lastCreatedAt) " +
               "AND c.status = 'APPROVED' " +
               "ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByCursor(@Param("postId") UUID postId, 
                                       @Param("lastCreatedAt") LocalDateTime lastCreatedAt, 
                                       Pageable pageable);
}
