package com.example.English.teaching.center.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.English.teaching.center.entity.Transaction;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Tìm lịch sử giao dịch của 1 user, sắp xếp mới nhất lên đầu
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Transaction> findByTxnRef(String txnRef);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Transaction t WHERE t.txnRef = :txnRef")
    Optional<Transaction> findByTxnRefForUpdate(@Param("txnRef") String txnRef);
}