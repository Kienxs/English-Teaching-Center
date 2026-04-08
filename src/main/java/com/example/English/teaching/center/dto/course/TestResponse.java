package com.example.English.teaching.center.dto.course;

import lombok.Data;
import java.util.List;

import com.example.English.teaching.center.utils.HtmlSanitizerUtils;

@Data
public class TestResponse {
    private Long id;
    private String title;
    private String slug;
    private Integer durationMinutes;
    private List<QuestionResponse> questions;
}