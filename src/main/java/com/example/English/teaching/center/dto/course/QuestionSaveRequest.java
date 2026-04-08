package com.example.English.teaching.center.dto.course;

import java.math.BigDecimal;
import java.util.List;

import com.example.English.teaching.center.entity.Question;
import com.example.English.teaching.center.utils.HtmlSanitizerUtils;

import lombok.Data;

@Data
public class QuestionSaveRequest {
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

    public void setQuestionText(String questionText){
        this.questionText = HtmlSanitizerUtils.sanitizeRichText(questionText);
    }
    public void setOptionA(String optionA) {
        this.optionA = HtmlSanitizerUtils.sanitizePlainText(optionA);
    }
    public void setOptionB(String optionB) {
        this.optionB = HtmlSanitizerUtils.sanitizePlainText(optionB);
    }
    public void setOptionC(String optionC) {
        this.optionC = HtmlSanitizerUtils.sanitizePlainText(optionC);
    }
    public void setOptionD(String optionD) {
        this.optionD = HtmlSanitizerUtils.sanitizePlainText(optionD);
    }
}