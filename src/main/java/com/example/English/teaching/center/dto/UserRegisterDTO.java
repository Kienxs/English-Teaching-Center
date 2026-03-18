package com.example.English.teaching.center.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDTO {
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    @Pattern(regexp = "\\d{10,12}", message = "Số điện thoại phải từ 10-12 số")
    private String phone;

    @Size(min = 8, message = "Mật khẩu phải từ 8 ký tự")
    private String password;
}
