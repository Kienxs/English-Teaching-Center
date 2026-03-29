package com.example.English.teaching.center.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "materials")
@Data
@NoArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude 
    private Lesson lesson;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "file_url", nullable = false, length = 255)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20) 
    private FileType type = FileType.PDF;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude 
    private Teacher uploadedBy;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    public enum FileType {
        PDF,
        VIDEO,
        DOC,
        PPT,
        SCORM,
        OTHER
    }
}