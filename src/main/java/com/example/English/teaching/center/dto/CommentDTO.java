package com.example.English.teaching.center.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class CommentDTO {
    private String userAvatar;
    private String userName;
    private String content;
    private String timeAgo;
}
