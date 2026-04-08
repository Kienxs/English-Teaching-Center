package com.example.English.teaching.center.dto.course;

import lombok.Data;

@Data
public class MaterialResponse{
    private Long id;
    private Long lessonId;
    private String title;
    private String fileUrl;
    private String type;
}