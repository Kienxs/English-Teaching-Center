package com.example.English.teaching.center.service.user;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.English.teaching.center.dto.user.PasswordChangeRequest;
import com.example.English.teaching.center.dto.user.UserNavbarDTO;
import com.example.English.teaching.center.dto.user.UserProfileRequest;
import com.example.English.teaching.center.dto.user.UserProfileResponse;
import com.example.English.teaching.center.dto.user.UsernameChangeRequest;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.exception.InvalidFileException;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.mapper.UserMapper;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.service.auth.RefreshTokenService;
import com.example.English.teaching.center.service.infra.CloudinaryService;
import com.example.English.teaching.center.service.infra.RateLimitingService;

import io.github.bucket4j.Bucket;
import jakarta.transaction.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;
    private final RateLimitingService rateLimitingService;
    private final RefreshTokenService refreshTokenService;

    public UserService(UserRepository userRepository, 
                PasswordEncoder passwordEncoder, 
                UserMapper userMapper,
                CloudinaryService cloudinaryService,
                RateLimitingService rateLimitingService,
                RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.cloudinaryService = cloudinaryService;
        this.rateLimitingService = rateLimitingService;
        this.refreshTokenService = refreshTokenService;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }

    public UserNavbarDTO getUserNavbarInfo(String email){
        User user = userRepository.findByEmail(email).orElse(null);

        return userMapper.toNavbarDTO(user);
    }

    public UserProfileResponse getUserProfile(String email){
        User user = findByEmail(email);
        UserProfileResponse dto = userMapper.toDTO(user);
        dto.setAvatarUrl(user.getAvatarUrl());

        return dto;
    }

    @Transactional
    public void updateUserProfile(String email, UserProfileRequest dto, MultipartFile avatarFile) {
        // 1. Kiểm tra Rate Limit
        String limitKey = "UPDATE_PROFILE_USER_" + email;

        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 5, 10);
        if(!bucket.tryConsume(1)) 
            throw new RateLimitException("Bạn thao tác quá nhanh! Thử lại sau 10 phút.");

        // 2. Kiểm tra File
        if(avatarFile != null && !avatarFile.isEmpty()){
            // 2.1. check dung lượng
            if(avatarFile.getSize() > 2 * 1024 * 1024)
                throw new InvalidFileException("File quá lớn! Vui lòng chọn ảnh dưới 2MB.");

            String contentType = avatarFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) 
                throw new InvalidFileException("Định dạng không hỗ trợ!");

            String originalFilename = avatarFile.getOriginalFilename();
            if(originalFilename == null || !originalFilename.contains("."))
                throw new InvalidFileException("File không có định dạng hợp lệ!");

            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

            if(!allowedExtensions.contains(extension))
                throw new InvalidFileException("Chỉ chấp nhận file ảnh có đuôi: JPG, JPEG, PNG, GIF, WEBP!");
        }

        User userToUpdate = findByEmail(email);
        String oldAvatarUrl = userToUpdate.getAvatarUrl();
        userMapper.updateEntityFromDTO(dto, userToUpdate);

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String newImageUrl = cloudinaryService.uploadFile(avatarFile);
            userToUpdate.setAvatarUrl(newImageUrl); 

            if (oldAvatarUrl != null) {
                String oldPublicId = cloudinaryService.extractPublicId(oldAvatarUrl);
                if (oldPublicId != null) {
                    try {
                        cloudinaryService.deleteFile(oldPublicId);
                    } catch (Exception e) {
                        System.err.println("Lỗi xóa ảnh cũ trên Cloudinary (có thể đã bị xóa trước đó): " + e.getMessage());
                    }
                }
            }
        }

        userRepository.save(userToUpdate);
    }

    public BigDecimal getUserBalance(String email){
        return userRepository.findByEmail(email)
        .map(user -> user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO)
        .orElse(BigDecimal.ZERO);
    }

    @Transactional
    public void changePassword(String email, PasswordChangeRequest dto) {
        String limitKey = "CHANGE_PASSWORD_USER_" + email;

        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 3, 15);
        if(!bucket.tryConsume(1)) 
            throw new RateLimitException("Bạn thao tác quá nhiều lần! Vui lòng thử lại sau 15 phút.");

        User user = findByEmail(email);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) 
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) 
            throw new RuntimeException("Mật khẩu mới và mật khẩu xác nhận không khớp");

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        refreshTokenService.deleteAllByUser(user);
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);
    }

    @Transactional
    public void changeUsername(String email, UsernameChangeRequest dto){
        String limitKey = "CHANGE_USERNAME_USER_" + email;

        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 3, 10);
        if(!bucket.tryConsume(1)) 
            throw new RateLimitException("Bạn thao tác quá nhiều lần! Vui lòng thử lại sau 10 phút.");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if(!passwordEncoder.matches(dto.getPasswordConfirm(), user.getPassword()))
            throw new RuntimeException("Mật khẩu xác nhận không chính xác");

        user.setFullName(dto.getNewUsername().trim());
        userRepository.save(user);
    }
}