package com.example.English.teaching.center.dto;

import lombok.Data;
import java.util.List;

@Data
public class TestDTO {
    private Long id;
    private String title;
    private String slug;
    private Integer durationMinutes;
    private List<QuestionDTO> questions;
}