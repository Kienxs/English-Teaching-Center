package com.example.English.teaching.center.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsernameChangeDTO {
    @NotBlank(message = "Tên hiển thị không được để trống")
    private String newUsername;

    @NotBlank(message = "Vui lòng nhập mật khẩu xác nhận")
    private String passwordConfirm;
}
