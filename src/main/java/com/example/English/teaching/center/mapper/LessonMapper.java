package com.example.English.teaching.center.mapper;

import org.springframework.stereotype.Component;

import com.example.English.teaching.center.dto.course.LessonDTO;
import com.example.English.teaching.center.entity.Lesson;

@Component
public class LessonMapper {
    public LessonDTO toDTO(Lesson entity){
        if(entity == null) return null;
        LessonDTO dto = new LessonDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setLessonOrder(entity.getLessonOrder());
        return dto;
    }
}