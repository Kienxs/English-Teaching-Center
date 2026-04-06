package com.example.English.teaching.center.dto.content;

import lombok.Data;

@Data
public class CourseCommentResponse {
    private String userAvatar;
    private String userName;
    private String content;
    private String timeAgo;
}
