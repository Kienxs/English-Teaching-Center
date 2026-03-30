package com.example.English.teaching.center.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.English.teaching.center.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c " +
               "WHERE c.post.id = :postId " +
               "AND (:lastId IS NULL OR c.id < :lastId) " +
               "AND c.status = 'APPROVED' " +
               "ORDER BY c.id DESC")
        List<Comment> findCommentsByCursor(@Param("postId") Long postId, 
                                          @Param("lastId") Long lastId, 
                                          Pageable pageable);
}
