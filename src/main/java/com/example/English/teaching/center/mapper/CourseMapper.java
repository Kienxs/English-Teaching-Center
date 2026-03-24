package com.example.English.teaching.center.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.English.teaching.center.dto.CourseDTO;
import com.example.English.teaching.center.entity.Course;

@Component
public class CourseMapper {
    private final LessonMapper lessonMapper;

    @Autowired
    public CourseMapper(LessonMapper lessonMapper) {
        this.lessonMapper = lessonMapper;
    }

    public CourseDTO toDTO(Course entity){
        if(entity == null) return null;

        CourseDTO dto = new CourseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setSlug(entity.getSlug());
        dto.setDescription(entity.getDescription());
        dto.setFee(entity.getFee());
        dto.setViewCount(entity.getViewCount() != null ? entity.getViewCount() : 0);
        dto.setCreatedAt(entity.getCreatedAt());

        if(entity.getCategory() != null) 
            dto.setCategory(entity.getCategory().name());

        if(entity.getMode() != null)
            dto.setMode(entity.getMode().name());

        dto.setImageUrl(entity.getImageUrl());
        dto.setDuration(entity.getDuration());

        if(entity.getStatus() != null)
            dto.setStatus(entity.getStatus().name());

        if(entity.getTeacher() != null){
            dto.setTeacherId(entity.getTeacher().getId());
            if(entity.getTeacher().getUser() != null)
                dto.setTeacherName(entity.getTeacher().getUser().getFullName());
        }

        if(entity.getLessons() != null){
            dto.setLessons(entity.getLessons().stream()
                .map(lessonMapper::toDTO)
                .toList());
        }

        return dto;
    }
}