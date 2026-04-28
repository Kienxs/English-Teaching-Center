package com.example.English.teaching.center.mapper;

import org.springframework.stereotype.Component;

import com.example.English.teaching.center.dto.content.CommentResponse;
import com.example.English.teaching.center.entity.Comment;

@Component
public class CommentMapper {
    public CommentResponse toDTO(Comment comment){
        if(comment == null) return null;

        CommentResponse dto = new CommentResponse();
        dto.setId(comment.getId());;
        dto.setContent(comment.getContent());

        if (comment.getUser() != null) 
            dto.setUserName(comment.getUser().getEmail());
        
        if (comment.getCreatedAt() != null) 
            dto.setCreatedAt(comment.getCreatedAt().toString());

        return dto;
    }
}