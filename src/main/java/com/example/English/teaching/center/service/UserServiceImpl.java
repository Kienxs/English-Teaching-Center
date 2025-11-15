package com.example.English.teaching.center.service; // (Nên tạo thư mục 'impl')

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.English.teaching.center.model.User;
import com.example.English.teaching.center.model.dto.PasswordChangeDTO;
import com.example.English.teaching.center.model.dto.UserProfileDTO;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.service.UserService; // Import interface

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService { // Implements interface

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection của bạn rất tốt!
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
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

    @Override
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

    // ----- CÁC PHƯƠNG THỨC MỚI ĐƯỢC THÊM VÀO -----

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }

    @Override
    public void updateUserProfile(String email, UserProfileDTO dto) {
        User userToUpdate = findByEmail(email); // Tái sử dụng hàm findByEmail
        
        userToUpdate.setName(dto.getFullName());
        userToUpdate.setPhone(dto.getPhone());
        
        // CẬP NHẬT CÁC TRƯỜNG KHÁC TỪ DTO (NẾU CÓ)
        // user.setDob(dto.getDob());
        // user.setCity(dto.getCity());
        
        userRepository.save(userToUpdate);
    }

    @Override
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