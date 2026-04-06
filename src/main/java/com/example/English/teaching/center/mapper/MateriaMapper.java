package com.example.English.teaching.center.mapper;

import org.springframework.stereotype.Component;

import com.example.English.teaching.center.dto.course.MaterialDTO;
import com.example.English.teaching.center.entity.Material;

@Component
public class MateriaMapper {
    public MaterialDTO toDTO(Material entity) {
        if (entity == null) {
            return null;
        }
        MaterialDTO dto = new MaterialDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setFileUrl(entity.getFileUrl());
        dto.setType(entity.getType().name());
        return dto;
    }
}
