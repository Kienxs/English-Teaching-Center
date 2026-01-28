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
public class TestResult {

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

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "submit_time")
    private LocalDateTime submitTime;

    @Column(name = "execution_time_seconds")
    private Integer executionTimeSeconds;

    @Enumerated(EnumType.STRING)
    private Status status; // Enum: DOING, COMPLETED

    // Helper method để hiển thị phút:giây
    public String getFormattedDuration() {
        if (executionTimeSeconds == null) return "00:00";
        long minutes = executionTimeSeconds / 60;
        long seconds = executionTimeSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Lob // Hoặc dùng @JdbcTypeCode(SqlTypes.JSON) nếu bạn cấu hình JSON
    private String details; // Lưu trữ chi tiết bài làm (ví dụ: JSON)

    public enum Status{
        DOING,
        COMPLETED
    }
}