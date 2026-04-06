package com.example.English.teaching.center.mapper;

import org.springframework.stereotype.Component;

import com.example.English.teaching.center.dto.content.PostListResponse;
import com.example.English.teaching.center.entity.Post;

@Component
public class PostMapper {
    public PostListResponse toListDTO(Post entity){
        if(entity == null) return null;

        PostListResponse dto = new PostListResponse();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setSlug(entity.getSlug());
        dto.setSummary(entity.getSummary());
        dto.setThumbnailUrl(entity.getThumbnailUrl());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setViewCount(entity.getViewCount());
        return dto;
    }
}
