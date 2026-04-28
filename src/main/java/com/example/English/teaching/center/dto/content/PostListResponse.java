package com.example.English.teaching.center.dto.content;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class PostListResponse {
    private UUID id;
    private String title;
    private String slug;
    private String summary;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private int viewCount;
}
