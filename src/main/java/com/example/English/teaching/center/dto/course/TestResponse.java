package com.example.English.teaching.center.dto.course;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class TestResponse {
    private UUID id;
    private String title;
    private String slug;
    private Integer durationMinutes;
    private List<QuestionResponse> questions;
}