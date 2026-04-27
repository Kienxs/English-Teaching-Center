package com.example.English.teaching.center.entity;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "post_sections")
@Data
public class PostSection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) 
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "CHAR(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "section_title")
    private String sectionTitle;

    @Column(name = "section_content", columnDefinition = "LONGTEXT")
    private String sectionContent;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(name = "section_order")
    private Integer sectionOrder = 0;
}
