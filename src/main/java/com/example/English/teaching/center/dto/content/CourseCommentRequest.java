package com.example.English.teaching.center.dto.content;

import java.util.UUID;

import com.example.English.teaching.center.utils.HtmlSanitizerUtils;

import lombok.Data;

@Data
public class CourseCommentRequest {
    private UUID courseId;
    private String text;

    public void setText(String text){
        this.text = HtmlSanitizerUtils.sanitizePlainText(text);
    }
}
