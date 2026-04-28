package com.example.English.teaching.center.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.English.teaching.center.entity.Test;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestRepository extends JpaRepository<Test, UUID> {
    @Query("SELECT t FROM Test t LEFT JOIN FETCH t.questions WHERE t.id = :testId")
    Optional<Test> findByIdWithQuestions(@Param("testId") UUID testId);

    boolean existsBySlugAndIdNot(String slug, UUID id); 

    Optional<Test> findBySlug(String slug);
}