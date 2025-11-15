package com.example.English.teaching.center.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_results")
@Data
@NoArgsConstructor
public class TestResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @CreationTimestamp
    @Column(name = "taken_at", updatable = false)
    private LocalDateTime takenAt;

    @Lob // Hoặc dùng @JdbcTypeCode(SqlTypes.JSON) nếu bạn cấu hình JSON
    private String details; // Lưu trữ chi tiết bài làm (ví dụ: JSON)
}