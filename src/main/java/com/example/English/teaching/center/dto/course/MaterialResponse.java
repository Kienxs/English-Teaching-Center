package com.example.English.teaching.center.dto.course;

import java.util.UUID;

import lombok.Data;

@Data
public class MaterialResponse{
    private UUID id;
    private UUID lessonId;
    private String title;
    private String fileUrl;
    private String type;
}