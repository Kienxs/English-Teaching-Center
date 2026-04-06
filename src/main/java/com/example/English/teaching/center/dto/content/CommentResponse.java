package com.example.English.teaching.center.dto.content;

import lombok.Data;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private String userName;
    private String createdAt;
}