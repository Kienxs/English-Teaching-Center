package com.example.English.teaching.center.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) 
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private UUID id;

    @Column(name = "txn_ref", unique = true, length = 100)
    private String txnRef;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "actual_amount", precision = 15, scale = 2)
    private BigDecimal actualAmount;
    
    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; 

    @Column(name = "gateway_tran_no")
    private String gatewayTranNo;

    @Column(name = "reference_id")
    private Long referenceId; 

    @Column(name = "description")
    private String description;

    // Dùng CreationTimestamp cho đồng bộ với các Entity khác
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
    
    public enum TransactionType {
        DEPOSIT, 
        PAYMENT, 
        REFUND  
    }

    public enum TransactionStatus{
        PENDING,
        SUCCESS,
        FAILED,
        CANCELLED,
        EXPIRED
    }
}