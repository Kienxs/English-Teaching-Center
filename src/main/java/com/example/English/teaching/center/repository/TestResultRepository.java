package com.example.English.teaching.center.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.English.teaching.center.model.TestResult;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByTestIdAndStudentIdOrderByTakenAtDesc(Long testId, Long studentId);

    List<TestResult> findByTestIdAndStudentIdAndStatus(Long testId, Long studentId, TestResult.Status status);

    List<TestResult> findByStudentIdAndStatus(Long studentId, TestResult.Status status);
}