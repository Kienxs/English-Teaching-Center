package com.example.English.teaching.center.dto.content;

import java.util.UUID;

import lombok.Data;

@Data
public class CommentResponse {
    private UUID id;
    private String content;
    private String userName;
    private String createdAt;
}