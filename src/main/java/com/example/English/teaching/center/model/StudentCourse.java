package com.example.English.teaching.center.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_courses",
    uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}))

@Data
@NoArgsConstructor

public class StudentCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @CreationTimestamp
    @Column(name = "enrolled_at", updatable = false)
    private LocalDateTime enrolledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ENROLLED;

    @CreationTimestamp
    @Column(name = "expires_at", updatable = false)
    private LocalDateTime ExpiresAt;

    public enum Status{
        ENROLLED,
        COMPLETED,
        CANCELLED
    }
}
