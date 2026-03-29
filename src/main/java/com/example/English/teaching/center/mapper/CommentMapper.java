package com.example.English.teaching.center.mapper;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.example.English.teaching.center.dto.CommentDTO;
import com.example.English.teaching.center.entity.CourseComment;

@Component
public class CommentMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public CommentDTO toDTO(CourseComment cmt){
        if(cmt == null) return null;

        CommentDTO dto = new CommentDTO();

        String userName = (cmt.getUser() != null) ? cmt.getUser().getFullName() : "Người dùng ẩn danh";
        String avatarUrl = (cmt.getUser() != null) ? cmt.getUser().getAvatarUrl() : "/images/default-avatar.png";

        dto.setUserName(userName);
        dto.setUserAvatar(avatarUrl);
        dto.setContent(cmt.getCommentText());

        if(cmt.getCreatedAt() != null)
            dto.setTimeAgo(cmt.getCreatedAt().format(FORMATTER));
        else
            dto.setTimeAgo("");

        return dto;
    }
}
