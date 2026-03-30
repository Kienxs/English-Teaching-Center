package com.example.English.teaching.center.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Long id;
    private String content;
    private String userName;
    private String createdAt;
}