package com.example.English.teaching.center.service;

import com.example.English.teaching.center.model.User;
import com.example.English.teaching.center.model.dto.PasswordChangeDTO;
import com.example.English.teaching.center.model.dto.UserProfileDTO;

public interface UserService {
    User register(User user);

    User login(String email, String rawPassword) throws IllegalAccessException;

    User findByEmail(String email);

    void updateUserProfile(String email, UserProfileDTO dto);

    void changePassword(String email, PasswordChangeDTO dto);
}