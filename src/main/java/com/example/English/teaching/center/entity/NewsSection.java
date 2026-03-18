package com.example.English.teaching.center.entity;

import jakarta.persistence.*;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "news_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_title", length = 255)
    private String sectionTitle;

    @Column(name = "section_content", columnDefinition = "TEXT")
    private String sectionContent;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "image_caption", length = 255)
    private String imageCaption;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Quan hệ ngược lại với News
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    @JsonIgnore // Quan trọng để tránh lỗi StackOverflow khi API trả về JSON
    private News news;
}
