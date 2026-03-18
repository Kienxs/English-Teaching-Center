package com.example.English.teaching.center.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.English.teaching.center.entity.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Tìm lịch sử giao dịch của 1 user, sắp xếp mới nhất lên đầu
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}