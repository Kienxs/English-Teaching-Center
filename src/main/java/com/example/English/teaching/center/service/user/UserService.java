package com.example.English.teaching.center.service.user;

import java.math.BigDecimal;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.English.teaching.center.dto.PasswordChangeDTO;
import com.example.English.teaching.center.dto.UserNavbarDTO;
import com.example.English.teaching.center.dto.UserProfileDTO;
import com.example.English.teaching.center.dto.UsernameChangeDTO;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.exception.InvalidFileException;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.mapper.UserMapper;
import com.example.English.teaching.center.repository.UserRepository;
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

    public UserService(UserRepository userRepository, 
            PasswordEncoder passwordEncoder, 
            UserMapper userMapper,
            CloudinaryService cloudinaryService,
            RateLimitingService rateLimitingService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.cloudinaryService = cloudinaryService;
        this.rateLimitingService = rateLimitingService;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));
    }

    public UserNavbarDTO getUserNavbarInfo(String email){
        User user = userRepository.findByEmail(email).orElse(null);

        return userMapper.toNavbarDTO(user);
    }

    public UserProfileDTO getUserProfile(String email){
        User user = findByEmail(email);
        return userMapper.toDTO(user);
    }

    @Transactional
    public void updateUserProfile(String email, UserProfileDTO dto, MultipartFile avatarFile) {
        // 1. Kiểm tra Rate Limit
        Bucket bucket = rateLimitingService.resolveBucket(email);
        if(!bucket.tryConsume(1)) {
            throw new RateLimitException("Bạn thao tác quá nhanh! Thử lại sau 10 phút.");
        }

        // 2. Kiểm tra File
        if(avatarFile != null && !avatarFile.isEmpty()){
            // Kiểm tra kích thước
            if(avatarFile.getSize() > 2 * 1024 * 1024){
                throw new InvalidFileException("File quá lớn! Vui lòng chọn ảnh dưới 2MB.");
            }
            
            // Kiểm tra định dạng
            String contentType = avatarFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new InvalidFileException("Định dạng không hỗ trợ!");
            }
        }

        User userToUpdate = findByEmail(email);
        String oldAvatarUrl = userToUpdate.getAvatarUrl();

        // 1. Cập nhật thông tin chữ trước
        userMapper.updateEntityFromDTO(dto, userToUpdate);

        // 2. Xử lý ảnh
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // Upload ảnh mới
            String newImageUrl = cloudinaryService.uploadFile(avatarFile);
            userToUpdate.setAvatarUrl(newImageUrl);

            // 3. Xóa ảnh cũ nếu nó tồn tại trên Cloudinary
            String oldPublicId = cloudinaryService.extractPublicId(oldAvatarUrl);
            if (oldPublicId != null) {
                cloudinaryService.deleteFile(oldPublicId);
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
    public void changePassword(String email, PasswordChangeDTO dto) {
        User user = findByEmail(email);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) 
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) 
            throw new RuntimeException("Mật khẩu mới và mật khẩu xác nhận không khớp");

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    @Transactional
    public void changeUsername(String email, UsernameChangeDTO dto){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if(!passwordEncoder.matches(dto.getPasswordConfirm(), user.getPassword()))
            throw new RuntimeException("Mật khẩu xác nhận không chính xác");

        user.setFullName(dto.getNewUsername().trim());
        userRepository.save(user);
    }
}