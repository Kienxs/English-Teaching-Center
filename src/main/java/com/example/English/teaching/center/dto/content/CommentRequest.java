package com.example.English.teaching.center.dto.content;

import com.example.English.teaching.center.utils.HtmlSanitizerUtils;

import lombok.Data;

@Data
public class CommentRequest {
    private String postSlug;
    private String content;

    public void setContent(String content){
        this.content = HtmlSanitizerUtils.sanitizePlainText(content);
    }
}
