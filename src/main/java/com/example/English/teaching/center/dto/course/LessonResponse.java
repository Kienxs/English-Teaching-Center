package com.example.English.teaching.center.dto.course;

import java.util.List;

import lombok.Data;

@Data
public class LessonResponse {
    private Long id;
    private String title;
    private String description;
    private Integer lessonOrder;
    private List<MaterialResponse> materials; 
    private List<TestResponse> tests;
}