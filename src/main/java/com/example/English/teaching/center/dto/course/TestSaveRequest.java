package com.example.English.teaching.center.dto.course;

import java.util.UUID;

import com.example.English.teaching.center.utils.HtmlSanitizerUtils;

import lombok.Data;

@Data
public class TestSaveRequest {
    private UUID id;
    private UUID lessonId;
    private String title;
    private Integer durationMinutes;

    public void setTitle(String title){
        this.title = HtmlSanitizerUtils.sanitizePlainText(title);
    }
}
