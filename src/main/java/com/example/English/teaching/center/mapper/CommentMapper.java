package com.example.English.teaching.center.mapper;

import org.springframework.stereotype.Component;

import com.example.English.teaching.center.dto.CommentDTO;
import com.example.English.teaching.center.entity.Comment;

@Component
public class CommentMapper {
    public CommentDTO toDTO(Comment comment){
        if(comment == null) return null;

        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());

        if (comment.getUser() != null) {
            dto.setUserName(comment.getUser().getEmail()); // Hoặc getFullName() tuỳ bạn
        }
        
        if (comment.getCreatedAt() != null) {
            dto.setCreatedAt(comment.getCreatedAt().toString());
        }

        return dto;
    }
}
