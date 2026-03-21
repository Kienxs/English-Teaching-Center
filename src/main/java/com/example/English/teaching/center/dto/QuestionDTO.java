package com.example.English.teaching.center.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class QuestionDTO {
    private Long id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private BigDecimal points;
}
