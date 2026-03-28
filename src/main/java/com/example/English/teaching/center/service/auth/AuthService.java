package com.example.English.teaching.center.service.auth;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.English.teaching.center.dto.UserLoginResponseDTO;
import com.example.English.teaching.center.dto.UserRegisterDTO;
import com.example.English.teaching.center.entity.RefreshToken;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.mapper.UserMapper;
import com.example.English.teaching.center.repository.UserRepository;
import com.example.English.teaching.center.service.infra.EmailService;
import com.example.English.teaching.center.service.infra.RateLimitingService;
import com.example.English.teaching.center.service.infra.ReCaptchaService;
import com.example.English.teaching.center.utils.JwtUtils;

import io.github.bucket4j.Bucket;
import jakarta.transaction.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RateLimitingService rateLimitingService;
    private final ReCaptchaService reCaptchaService;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository, 
                       PasswordEncoder passwordEncoder, 
                       UserMapper userMapper,
                       RateLimitingService rateLimitingService,
                       ReCaptchaService reCaptchaService,
                       EmailService emailService,
                       JwtUtils jwtUtils,
                       RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.rateLimitingService = rateLimitingService;
        this.reCaptchaService = reCaptchaService;
        this.emailService = emailService;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }
// Register -------------------------------
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

// login -----------------------------------
    public User authenticateOnly(String email, String rawPassword) throws IllegalAccessException {
    // 1. Rate Limit 
    Bucket bucket = rateLimitingService.resolveBucket("login_" + email); 
    if (!bucket.tryConsume(1)) {
        throw new RateLimitException("Bạn đã nhập sai quá nhiều lần. Vui lòng thử lại sau 10 phút.");
    }

    // 2. Tìm user
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
    
    return user; 
}

// Process forgot password -------------------------------------
    @Transactional
    public void generatePasswordResetToken(String email) throws Exception{
        Bucket bucket = rateLimitingService.resolveBucket("forgot_" + email);
        if (!bucket.tryConsume(1)) {
            throw new RateLimitException("Bạn thao tác quá nhanh! Vui lòng thử lại sau 10 phút.");
        }

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
}
