package com.example.English.teaching.center.dto.user;

import com.example.English.teaching.center.utils.HtmlSanitizerUtils;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsernameChangeRequest {
    @NotBlank(message = "Tên hiển thị không được để trống")
    private String newUsername;

    @NotBlank(message = "Vui lòng nhập mật khẩu xác nhận")
    private String passwordConfirm;

    public void setNewUsername(String newUsername){
        this.newUsername = HtmlSanitizerUtils.sanitizePlainText(newUsername);
    }
}
