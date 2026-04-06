package com.example.English.teaching.center.dto.course;

import java.math.BigDecimal;
import java.util.List;

import com.example.English.teaching.center.entity.Question;

import lombok.Data;

@Data
public class QuestionSaveDTO {
    private Long id;
    private Long testId;
    private String questionText; 
    private List<Question.Option> options;
    private BigDecimal points;

    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer; 
}