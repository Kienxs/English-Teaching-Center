package com.example.English.teaching.center.repository;

import com.example.English.teaching.center.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Tìm lịch sử giao dịch của 1 user, sắp xếp mới nhất lên đầu
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
}