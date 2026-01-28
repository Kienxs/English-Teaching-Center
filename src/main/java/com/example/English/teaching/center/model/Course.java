package com.example.English.teaching.center.model;

import jakarta.persistence.*;
import lombok.Data; // Thêm Lombok Data
import lombok.NoArgsConstructor; // Thêm Lombok NoArgsConstructor
import org.hibernate.annotations.CreationTimestamp; // Thay thế @PrePersist thủ công

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List; // Cần thiết cho @OneToMany

@Entity
@Table(name = "courses")
@Data // Tự động tạo getters, setters, toString, equals, hashCode
@NoArgsConstructor // Tự động tạo constructor không tham số
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;
    
    @Column(name = "slug", nullable = false, length = 150)
    private String slug;

    @Lob // Tối ưu cho trường TEXT (description)
    @Column(name = "description")
    private String description;

    @Column(name = "category")
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "mode")
    @Enumerated(EnumType.STRING)
    private Mode mode;

    @Column(name = "duration", length = 50)
    private String duration;

    @Column(name = "access_period_days")
    private Integer accessPeriodDays;

    @Column(name = "fee", precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "view_count")
    private Integer viewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    @JsonIgnore
    private Teacher teacher;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Lob 
    @Column(name = "admin_note")
    private String adminNote;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<StudentCourse> studentCourses;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private List<CourseComments> courseComments;

    @CreationTimestamp 
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; 

    public enum Category{
        IELTS,
        TOEIC,
        KIDS,
        OTHER;
    }

    public enum Mode{
        ONLINE,
        OFFLINE;
    }

    public enum Status{
        DRAFT, 
        PENDING, 
        APPROVED, 
        REJECTED, 
        HIDDEN;
    }
}