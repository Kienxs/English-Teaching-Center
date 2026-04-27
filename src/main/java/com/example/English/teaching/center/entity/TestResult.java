package com.example.English.teaching.center.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "test_results")
@Data
@NoArgsConstructor
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) 
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude 
    private Test test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude 
    private User student;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @CreationTimestamp
    @Column(name = "taken_at", updatable = false)
    private LocalDateTime takenAt;

    @CreationTimestamp
    @Column(name = "start_time", updatable = false)
    private LocalDateTime startTime;

    @Column(name = "submit_time")
    private LocalDateTime submitTime;

    @Column(name = "execution_time_seconds")
    private Integer executionTimeSeconds = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status = Status.DOING; 

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "JSON")
    private List<AnswerDetail> details;

    public String getFormattedDuration() {
        if (executionTimeSeconds == null) return "00:00";
        long minutes = executionTimeSeconds / 60;
        long seconds = executionTimeSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public enum Status {
        DOING,
        COMPLETED
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDetail {
        private Long questionId;
        private String selectedAnswer; 
        @JsonProperty("isCorrect")
        private boolean isCorrect;
    }
}