package com.example.English.teaching.center.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class CourseDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal fee;
    private String category;
    private String status;
    private Integer viewCount;
    private LocalDateTime createdAt;

    private Long teacherId;
    private String teacherName;
    
    private List<LessonDTO> lessons;
}
