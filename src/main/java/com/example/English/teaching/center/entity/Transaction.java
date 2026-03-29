package com.example.English.teaching.center.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data 
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; 

    @Column(name = "reference_id")
    private Long referenceId; 

    @Column(name = "description")
    private String description;

    // Dùng CreationTimestamp cho đồng bộ với các Entity khác
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    public enum TransactionType {
        DEPOSIT, 
        PAYMENT, 
        REFUND  
    }
}