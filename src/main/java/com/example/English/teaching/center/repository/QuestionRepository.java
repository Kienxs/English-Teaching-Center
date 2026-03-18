package com.example.English.teaching.center.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.English.teaching.center.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long>{
    List<Question> findByTestId(Long testId);
}
