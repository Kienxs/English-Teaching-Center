package com.example.English.teaching.center.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.English.teaching.center.entity.TestResult;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, UUID> {
    
    List<TestResult> findByTestIdAndStudentIdOrderByTakenAtDesc(UUID testId, UUID studentId);

    List<TestResult> findByTestIdAndStudentIdAndStatus(UUID testId, UUID studentId, TestResult.Status status);

    List<TestResult> findByStudentIdAndStatus(UUID studentId, TestResult.Status status);
}