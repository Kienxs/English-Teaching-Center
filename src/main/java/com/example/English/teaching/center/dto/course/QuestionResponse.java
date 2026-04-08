package com.example.English.teaching.center.dto.course;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class QuestionResponse {
    private Long id;
    private String questionText;
    private BigDecimal points;
    private List<String> options;
}
