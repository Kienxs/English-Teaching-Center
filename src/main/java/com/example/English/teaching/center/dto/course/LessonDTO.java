package com.example.English.teaching.center.dto.course;

import java.util.List;

import lombok.Data;

@Data
public class LessonDTO {
    private Long id;
    private String title;
    private String description;
    private Integer lessonOrder;
    private List<MaterialDTO> materials; 
    private List<TestDTO> tests;
}