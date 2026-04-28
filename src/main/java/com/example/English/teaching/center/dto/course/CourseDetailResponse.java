package com.example.English.teaching.center.dto.course;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class CourseDetailResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal fee;
    private String category;
    private String mode;
    private String imageUrl;
    private String duration;
    private String status;
    private Integer viewCount;
    private LocalDateTime createdAt;

    private UUID teacherId;
    private String teacherName;
    
    private List<LessonResponse> lessons;
}