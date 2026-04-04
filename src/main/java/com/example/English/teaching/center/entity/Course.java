package com.example.English.teaching.center.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction; 

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "courses")
@Data 
@NoArgsConstructor 
@SQLDelete(sql = "UPDATE courses SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted = false")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "slug", nullable = false, length = 255)
    private String slug;

    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 20)
    private Category category = Category.OTHER; 

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", length = 20)
    private Mode mode = Mode.ONLINE; 

    @Column(name = "duration", length = 50)
    private String duration;

    @Column(name = "access_period_days")
    private Integer accessPeriodDays;

    @Column(name = "fee", precision = 10, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO; 

    @Column(name = "image_url", columnDefinition = "LONGTEXT")
    private String imageUrl;

    @Column(name = "view_count")
    private Integer viewCount = 0; 

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private Status status = Status.DRAFT;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @CreationTimestamp 
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Teacher teacher;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Lesson> lessons;
    
    @OneToMany(mappedBy = "course", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<StudentCourse> studentCourses;
    
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<CourseComment> courseComments; 

    public enum Category {
        IELTS, TOEIC, KIDS, OTHER;
    }

    public enum Mode {
        ONLINE, OFFLINE;
    }

    public enum Status {
        DRAFT, PENDING, APPROVED, REJECTED, HIDDEN;
    }
}