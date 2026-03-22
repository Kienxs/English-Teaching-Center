package com.example.English.teaching.center.dto;

import lombok.Data;

@Data
public class TestSaveDTO {
    private Long id;
    private Long lessonId;
    private String title;
    private Integer durationMinutes;
}
