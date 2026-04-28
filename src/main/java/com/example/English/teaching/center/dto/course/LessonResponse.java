package com.example.English.teaching.center.dto.course;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class LessonResponse {
    private UUID id;
    private String title;
    private String description;
    private Integer lessonOrder;
    private List<MaterialResponse> materials; 
    private List<TestResponse> tests;
}