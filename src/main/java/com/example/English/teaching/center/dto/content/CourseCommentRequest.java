package com.example.English.teaching.center.dto.content;

import com.example.English.teaching.center.utils.HtmlSanitizerUtils;

import lombok.Data;

@Data
public class CourseCommentRequest {
    private Long courseId;
    private String text;

    public void setText(String text){
        this.text = HtmlSanitizerUtils.sanitizePlainText(text);
    }
}
