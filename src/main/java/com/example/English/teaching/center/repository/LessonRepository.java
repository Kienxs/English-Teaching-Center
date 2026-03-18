package com.example.English.teaching.center.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.English.teaching.center.entity.Lesson;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.materials m WHERE l.id = :lessonId ORDER BY m.type DESC, m.title ASC")
    Optional<Lesson> findByIdWithMaterials(@Param("lessonId") Long lessonId);

    @Query("SELECT l FROM Lesson l LEFT JOIN FETCH l.tests WHERE l.id = :id")
    Lesson findByIdWithTests(@Param("id") Long id);
}
