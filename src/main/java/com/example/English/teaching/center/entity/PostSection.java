package com.example.English.teaching.center.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "post_sections")
@Data
public class PostSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "section_title")
    private String sectionTitle;

    @Column(name = "section_content", columnDefinition = "LONGTEXT")
    private String sectionContent;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "section_order")
    private Integer sectionOrder = 0;
}
