package com.example.English.teaching.center.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class QuestionSaveDTO {
    private Long id;
    private Long testId;
    private String questionText; // Hoặc questionText tùy bạn đặt ở HTML
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private BigDecimal points;
}