package com.example.English.teaching.center.dto;

import lombok.Data;

@Data
public class LessonDTO {
    private Long id;
    private String title;
    private String description;
    private Integer lessonOrder;
    // Dùng MaterialDTO và TestDTO an toàn (đã giấu đáp án)
    private List<MaterialDTO> materials; 
    private List<TestDTO> tests;
}


@Data
public class LessonSaveDTO {
    private Long id;
    private Long courseId;
    private String title;
    private Integer lessonOrder;
    private String description;
}