package com.example.English.teaching.center.dto.report;

import com.example.English.teaching.center.utils.HtmlSanitizerUtils;

import lombok.Data;

@Data
public class TeacherProfileRequest {
    private String bio;
    private String expertise;
    private String linkedinUrl;

    public void setBio(String bio){
        this.bio = HtmlSanitizerUtils.sanitizePlainText(bio);
    }

    public void setExpertise(String expertise){
        this.expertise = HtmlSanitizerUtils.sanitizePlainText(expertise);
    }

    public void setLinkedinUrl(String linkedinUrl){
        this.linkedinUrl = HtmlSanitizerUtils.sanitizePlainText(linkedinUrl);
    }
}
