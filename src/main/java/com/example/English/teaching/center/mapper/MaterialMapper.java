package com.example.English.teaching.center.mapper;

import org.springframework.stereotype.Component;

import com.example.English.teaching.center.dto.course.MaterialResponse;
import com.example.English.teaching.center.entity.Material;

@Component
public class MaterialMapper {
    public MaterialResponse toDTO(Material entity) {
        if (entity == null) {
            return null;
        }
        MaterialResponse dto = new MaterialResponse();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setFileUrl(entity.getFileUrl());
        dto.setType(entity.getType().name());

        if (entity.getLesson() != null) 
            dto.setLessonId(entity.getLesson().getId());

        return dto;
    }
}