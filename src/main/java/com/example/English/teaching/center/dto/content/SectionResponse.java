package com.example.English.teaching.center.dto.content;

import com.example.English.teaching.center.utils.HtmlSanitizerUtils;

import lombok.Data;

@Data
public class SectionResponse {
    private Long id;
    private String sectionTitle;
    private String sectionContent;
    private String imageUrl;
    private Integer sectionOrder; 

    public void setSectionTitle(String sectionTitle) {
        this.sectionTitle = HtmlSanitizerUtils.sanitizePlainText(sectionTitle);
    }

    public void setSectionContent(String sectionContent) {
        this.sectionContent = HtmlSanitizerUtils.sanitizeRichText(sectionContent);
    }
}