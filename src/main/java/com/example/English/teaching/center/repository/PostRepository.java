package com.example.English.teaching.center.repository;

import com.example.English.teaching.center.entity.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByTypeAndStatusOrderByCreatedAtDesc(Post.PostType type, Post.PostStatus status, Pageable pageable);

    Optional<Post> findBySlugAndStatus(String slug, Post.PostStatus status);

    @Query("SELECT p FROM Post p WHERE p.type = :type " +
       "AND p.status = 'APPROVED' " +
       "AND p.id != :currentId " + 
       "ORDER BY p.createdAt DESC")
    List<Post> findRelatedPosts(@Param("type") Post.PostType type, 
                                @Param("currentId") Long currentId, 
                                Pageable pageable);
}