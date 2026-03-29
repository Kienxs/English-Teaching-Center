package com.example.English.teaching.center.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PostListDTO {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private int viewCount;
}
