package com.example.English.teaching.center.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.English.teaching.center.dto.PasswordChangeDTO;
import com.example.English.teaching.center.dto.UserLoginResponseDTO;
import com.example.English.teaching.center.dto.UserNavbarDTO;
import com.example.English.teaching.center.dto.UserProfileDTO;
import com.example.English.teaching.center.dto.UserRegisterDTO;
import com.example.English.teaching.center.dto.UsernameChangeDTO;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.exception.InvalidFileException;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.mapper.UserMapper;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.securty.ReCaptchaService;

import io.github.bucket4j.Bucket;
import jakarta.transaction.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;
    private final RateLimitingService rateLimitingService;
    private final ReCaptchaService reCaptchaService;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, 
            PasswordEncoder passwordEncoder, 
            UserMapper userMapper,
            CloudinaryService cloudinaryService,
            RateLimitingService rateLimitingService,
            ReCaptchaService reCaptchaService,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.cloudinaryService = cloudinaryService;
        this.rateLimitingService = rateLimitingService;
        this.reCaptchaService = reCaptchaService;
        this.emailService = emailService;
    }

    @Transactional
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

        // 5. Create code verify Email 
        String verificationCode = UUID.randomUUID().toString();
        user.setVerificationCode(verificationCode);

        userRepository.save(user);

        try{
            emailService.sendVerificationEmail(user.getEmail(), user.getFullName(), user.getVerificationCode());
        }catch(Exception e){
            System.err.println(("Lỗi khi gửi email xác nhận cho: " + user.getEmail()));
            e.printStackTrace();
        }
    }

    @Transactional
    public boolean verifyEmail(String verificationCode){
        User user = userRepository.findByVerificationCode(verificationCode).orElse(null);

        if(user == null || user.getStatus() == User.Status.ACTIVE) 
            return false;

        user.setVerificationCode(null);
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);

        return true;
    }

    public UserLoginResponseDTO login(String email, String rawPassword) throws IllegalAccessException {
        // 1. Rate Limit
        Bucket bucket = rateLimitingService.resolveBucket("login_" + email); 
        if (!bucket.tryConsume(1)) {
            throw new RateLimitException("Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau 10 phút.");
        }

        // 2. Find user
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalAccessException("Email không tồn tại!"));

        // 3. Check if the account has been activated
        if(user.getStatus() == User.Status.PENDING){
            throw new IllegalAccessException("Tài khoản chưa được kích hoạt. Vui lòng kiểm tra email của bạn!");
        }

        // 4. Check password
        if(!passwordEncoder.matches(rawPassword, user.getPassword())){
            throw new IllegalAccessException("Mật khẩu không đúng!");
        }
        
        return userMapper.toLoginResponseDTO(user, null);
    }

// Process forgot password -------------------------------------
    @Transactional
    public void generatePasswordResetToken(String email) throws Exception{
        User user = userRepository.findByEmail(email).orElse(null);
        if(user != null){
            String token = UUID.randomUUID().toString();
            user.setResetPasswordToken(token);
            userRepository.save(user);

            String resetPasswordLink = "http://localhost:8080/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(email, resetPasswordLink);
        }else{
            throw new Exception("Nếu email hợp lệ, một đường link khôi phục đã được gửi đi.");
        }
    }

    public User getByResetPasswordToken(String token){
        return userRepository.findByResetPasswordToken(token).orElse(null);
    }

    @Transactional
    public void updatePasswordByToken(User user, String newPassword){
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        userRepository.save(user);
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