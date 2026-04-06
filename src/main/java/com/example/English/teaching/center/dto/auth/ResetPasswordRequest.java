package com.example.English.teaching.center.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Token không được để trống")
    private String token;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min=8, message = "Mật khẩu phải từ 8 ký tự")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&._-]{8,}$",
             message = "Mật khẩu phải có ít nhất 8 ký tự, chứa chữ hoa, chữ thường, số và ký tự đặc biệt!")
    private String password;
}
