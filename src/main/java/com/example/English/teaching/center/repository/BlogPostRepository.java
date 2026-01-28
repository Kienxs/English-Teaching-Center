package com.example.English.teaching.center.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.English.teaching.center.model.BlogPost;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

// Process display for teacher -----------------------------------------------------------------------------------
    Page<BlogPost> findByAuthor_Id(Long authorId, Pageable pageable);

    List<BlogPost> findByAuthor_IdOrderByCreatedAtDesc(Long authorId);

    Optional<BlogPost> findBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

// Process display for admin ---------------------------------------------------------------
    Page<BlogPost> findByStatus(String status, Pageable pageable);
}
