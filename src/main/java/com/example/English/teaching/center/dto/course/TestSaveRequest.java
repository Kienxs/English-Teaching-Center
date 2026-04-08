package com.example.English.teaching.center.dto.course;

import com.example.English.teaching.center.utils.HtmlSanitizerUtils;

import lombok.Data;

@Data
public class TestSaveRequest {
    private Long id;
    private Long lessonId;
    private String title;
    private Integer durationMinutes;

    public void setTitle(String title){
        this.title = HtmlSanitizerUtils.sanitizePlainText(title);
    }
}
