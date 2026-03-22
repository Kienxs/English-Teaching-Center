package com.example.English.teaching.center.dto;

import lombok.Data;

@Data
public class MaterialDTO {
    private Long id;
    private Long lessonId;
    private String title;
    private String fileUrl;
    private String type;
}
