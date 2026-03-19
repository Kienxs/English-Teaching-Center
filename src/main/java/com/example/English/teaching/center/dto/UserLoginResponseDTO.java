package com.example.English.teaching.center.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private String role;
    private String accessToken;
}
