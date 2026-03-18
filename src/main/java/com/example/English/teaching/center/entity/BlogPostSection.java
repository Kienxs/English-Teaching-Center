package com.example.English.teaching.center.entity;

import jakarta.persistence.*;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "blog_post_sections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPostSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "section_title")
    private String sectionTitle;

    @Column(name = "section_content", columnDefinition = "TEXT")
    private String sectionContent;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "image_caption")
    private String imageCaption;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    // Quan hệ ngược lại với BlogPost
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore // Tránh lặp vô tận khi convert sang JSON
    private BlogPost blogPost;
}