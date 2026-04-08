package com.example.English.teaching.center.dto.user;

import com.example.English.teaching.center.utils.HtmlSanitizerUtils;

import lombok.Data;

@Data
public class UserProfileRequest {
    private String fullName;
    private String phone;
    private String avatarUrl;

    public void setFullName(String fullName) {
        this.fullName = HtmlSanitizerUtils.sanitizePlainText(fullName);
    }
}