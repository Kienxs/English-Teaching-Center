package com.example.English.teaching.center.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class PasswordChangeDTO {
     private String currentPassword;
     private String newPassword;
     private String confirmPassword;
}
