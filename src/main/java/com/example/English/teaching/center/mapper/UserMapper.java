package com.example.English.teaching.center.mapper;

import org.springframework.stereotype.Component;

import com.example.English.teaching.center.dto.UserLoginResponseDTO;
import com.example.English.teaching.center.dto.UserNavbarDTO;
import com.example.English.teaching.center.dto.UserProfileDTO;
import com.example.English.teaching.center.dto.UserRegisterDTO;
import com.example.English.teaching.center.entity.User;

@Component
public class UserMapper {
    public UserProfileDTO toDTO(User entity){
        if(entity == null) return null;

        UserProfileDTO dto = new UserProfileDTO();
        dto.setFullName(entity.getFullName());
        dto.setPhone(entity.getPhone());

        return dto;
    }

    public void updateEntityFromDTO(UserProfileDTO dto, User entity){
        if(dto == null || entity == null) return;

        if(dto.getFullName() != null)
            entity.setFullName(dto.getFullName());
        if(dto.getPhone() != null)
            entity.setPhone(dto.getPhone());
    }

    public User toEntity(UserRegisterDTO dto){
        if(dto == null) return null;

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPassword(dto.getPassword());
        return user;
    }

    public UserLoginResponseDTO toLoginResponseDTO(User user, String accessToken){
        if(user == null) return null;
        return UserLoginResponseDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole().name())
            .accessToken(accessToken)
            .build();
    }

    public UserNavbarDTO toNavbarDTO(User user){
        if(user == null) return null;

        return new UserNavbarDTO(user.getAvatarUrl());
    }
}
