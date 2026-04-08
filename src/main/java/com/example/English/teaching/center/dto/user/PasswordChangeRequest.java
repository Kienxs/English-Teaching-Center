package com.example.English.teaching.center.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeRequest {
     @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại")
     private String currentPassword;

     @NotBlank(message = "Mật khẩu mới không được để trống")
     @Size(min = 8, message = "Mật khẩu phải từ 8 ký tự")
     @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&._-])[A-Za-z\\d@$!%*?&._-]{8,}$",
              message = "Mật khẩu phải có ít nhất 8 ký tự, chứa chữ hoa, chữ thường, số và ký tự đặc biệt!")
     private String newPassword;

     @NotBlank(message = "Vui lòng xác nhận mật khẩu")
     private String confirmPassword;
}