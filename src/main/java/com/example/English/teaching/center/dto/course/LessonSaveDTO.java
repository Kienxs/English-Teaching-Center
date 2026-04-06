package com.example.English.teaching.center.dto.course;

import lombok.Data;

@Data
public class LessonSaveDTO {
    private Long id;
    private Long courseId;
    private String title;
    private Integer lessonOrder;
    private String description;
}
