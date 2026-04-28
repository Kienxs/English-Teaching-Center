package com.example.English.teaching.center.dto.course;

import java.util.UUID;

import com.example.English.teaching.center.utils.HtmlSanitizerUtils;

import lombok.Data;

@Data
public class LessonSaveRequest {
    private UUID id;
    private UUID courseId;
    private String title;
    private Integer lessonOrder;
    private String description;

    public void setTitle(String title){
        this.title = HtmlSanitizerUtils.sanitizePlainText(title);
    }

    public void setDescription(String description){
        this.description = HtmlSanitizerUtils.sanitizeRichText(description);
    }
}
