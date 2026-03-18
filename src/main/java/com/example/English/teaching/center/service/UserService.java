package com.example.English.teaching.center.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.English.teaching.center.dto.PasswordChangeDTO;
import com.example.English.teaching.center.dto.UserProfileDTO;
import com.example.English.teaching.center.dto.UserRegisterDTO;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.exception.InvalidFileException;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.mapper.UserMapper;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.securty.ReCaptchaService;

import io.github.bucket4j.Bucket;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;
    private final RateLimitingService rateLimitingService;
    private final ReCaptchaService reCaptchaService;

    public UserService(UserRepository userRepository, 
            PasswordEncoder passwordEncoder, 
            UserMapper userMapper,
            CloudinaryService cloudinaryService,
            RateLimitingService rateLimitingService,
            ReCaptchaService reCaptchaService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.cloudinaryService = cloudinaryService;
        this.rateLimitingService = rateLimitingService;
        this.reCaptchaService = reCaptchaService;
    }

    public void registerNewUser(UserRegisterDTO dto, String recaptchaResponse) {
        // 1. Kiểm tra Rate Limit (Chống spam đăng ký)
        Bucket bucket = rateLimitingService.resolveBucket(dto.getEmail());
        if (!bucket.tryConsume(1)) {
            throw new RateLimitException("Bạn thao tác quá nhanh! Vui lòng thử lại sau 10 phút.");
        }

        // 2. Xác thực reCAPTCHA
        if (!reCaptchaService.verify(recaptchaResponse)) {
            throw new RuntimeException("Xác thực robot thất bại. Vui lòng thử lại!");
        }

        User user = userMapper.toEntity(dto);

        // 3. Kiểm tra trùng lặp dữ liệu
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại!");
        }

        // 4. Mã hóa mật khẩu và lưu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.STUDENT);
        user.setStatus(User.Status.PENDING);
        user.setBalance(BigDecimal.ZERO);

        userRepository.save(user);
    }

    public User login(String email, String rawPassword) throws IllegalAccessException {
        Bucket bucket = rateLimitingService.resolveBucket("login_" + email); 
        if (!bucket.tryConsume(1)) {
            throw new RateLimitException("Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau 10 phút.");
        }

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
        return userMapper.toDTO(user);
    }

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