package com.example.English.teaching.center.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.English.teaching.center.entity.Transaction;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    // Tìm lịch sử giao dịch của 1 user, sắp xếp mới nhất lên đầu
    List<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Transaction> findByTxnRef(String txnRef);

    // Xử lý chống Race Condition khi nhận IPN/Webhook từ cổng thanh toán
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Transaction t WHERE t.txnRef = :txnRef")
    Optional<Transaction> findByTxnRefForUpdate(@Param("txnRef") String txnRef);
}