package com.example.English.teaching.center.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.English.teaching.center.model.User;
import com.example.English.teaching.center.model.dto.PasswordChangeDTO;
import com.example.English.teaching.center.model.dto.UserProfileDTO;
import com.example.English.teaching.center.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(User user) {
        if(userRepository.findByEmail(user.getEmail()).isPresent()){
            throw new IllegalArgumentException("Email đã được sử dụng!");
        }
        if (userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Số điện thoại đã được sử dụng!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(User.Role.STUDENT);
        }

        return userRepository.save(user);
    }

    public User login(String email, String rawPassword) throws IllegalAccessException {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (!optionalUser.isPresent()) {
            throw new IllegalAccessException("Email không chính xác");
        }

        User user = optionalUser.get();
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalAccessException("Mật khẩu không đúng");
        }
        
        return user;
    }


    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }

    public UserProfileDTO getUserProfile(String email){
        User user = findByEmail(email);
        UserProfileDTO dto = new UserProfileDTO();
        dto.setFullName(user.getName());
        dto.setPhone(user.getPhone());
        // dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }

    public void updateUserProfile(String email, UserProfileDTO dto, MultipartFile avatarFile) {
        User userToUpdate = findByEmail(email); 
        
        userToUpdate.setName(dto.getFullName());
        userToUpdate.setPhone(dto.getPhone());
    
        if(avatarFile != null && !avatarFile.isEmpty()){
            //String fileName = fileStorageService.storeFile(avatarFile);
            // userToUpdate.setAvatarUrl(fileName);
        }
        
        userRepository.save(userToUpdate);
    }

    public BigDecimal getUserBalance(String email){
        return userRepository.findByEmail(email)
        .map(user -> user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO)
        .orElse(BigDecimal.ZERO);
    }

    public void changePassword(String email, PasswordChangeDTO dto) {
        User user = findByEmail(email);

        // 1. Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }

        // 2. Kiểm tra mật khẩu mới và xác nhận
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới và mật khẩu xác nhận không khớp");
        }

        // 3. Cập nhật mật khẩu mới (đã mã hóa)
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}