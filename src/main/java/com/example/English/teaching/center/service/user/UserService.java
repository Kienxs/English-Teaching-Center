package com.example.English.teaching.center.service.user;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.tika.Tika;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.English.teaching.center.dto.user.*;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        
        String limitKey = "UPDATE_PROFILE_USER_" + email;
        Bucket bucket = rateLimitingService.resolveBucket(limitKey, 5, 10);
        if(!bucket.tryConsume(1)) throw new RateLimitException("Bạn thao tác quá nhanh! Thử lại sau 10 phút.");

        String newPublicId = null;
        String newImageUrl = null;

        if(avatarFile != null && !avatarFile.isEmpty()){
            
            // Extension check & Size 
            if(avatarFile.getSize() > 2 * 1024 * 1024) 
                throw new InvalidFileException("File quá lớn! Vui lòng chọn ảnh dưới 2MB.");

            String originalFilename = avatarFile.getOriginalFilename();
            if(originalFilename == null) throw new InvalidFileException("Tên file không hợp lệ!");
            
            String cleanFileName = StringUtils.cleanPath(originalFilename);
            if(cleanFileName.contains("..") || cleanFileName.contains("\0"))
                throw new SecurityException("Cảnh báo: Phát hiện tên file chứa ký tự độc hại!");

            int dotIndex = cleanFileName.lastIndexOf(".");
            if(dotIndex == -1 || dotIndex == cleanFileName.length() - 1)
                throw new InvalidFileException("File không có định dạng hợp lệ!");

            String extension = cleanFileName.substring(dotIndex + 1).toLowerCase();
            List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "webp");
            if(!allowedExtensions.contains(extension))
                throw new InvalidFileException("Chỉ chấp nhận file ảnh: JPG, JPEG, PNG, WEBP!");

            // Magic bytes check (Real type)
            Tika tika = new Tika();
            try (InputStream is = avatarFile.getInputStream()) {
                String detectedMimeType = tika.detect(is);

                List<String> strictMimeTypes = Arrays.asList("image/jpeg", "image/png", "image/webp");

                if (detectedMimeType == null || !strictMimeTypes.contains(detectedMimeType)) {
                    log.warn("Phát hiện file nghi ngờ giả mạo định dạng. User: {}, MIME: {}", email, detectedMimeType);
                    throw new InvalidFileException("Định dạng file bị từ chối. Vui lòng sử dụng JPG, PNG hoặc WEBP chuẩn!");
                }
            } catch (Exception e) {
                throw new InvalidFileException("Không thể phân tích định dạng thực sự của file!");
            }
            
            // Decode file & Check dimensions
            try (InputStream input = avatarFile.getInputStream();
                 ImageInputStream iis = ImageIO.createImageInputStream(input)) {
                
                if (iis == null) throw new InvalidFileException("Dữ liệu Header ảnh bị hỏng!");

                Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                if (!readers.hasNext()) throw new InvalidFileException("Định dạng ảnh không được hỗ trợ hoặc chứa mã độc!");

                ImageReader reader = readers.next();
                try {
                    reader.setInput(iis, true, true);
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);

                    if (width > 3000 || height > 4000) {
                        throw new InvalidFileException("Kích thước ảnh quá lớn (Tối đa Rộng: 3000px, Cao: 4000px).");
                    }
                } finally {
                    reader.dispose(); 
                }
            } catch (Exception e) {
                throw new InvalidFileException("Lỗi trong quá trình đọc thông số ảnh!");
            }

            // Sanitize, Rename (UUID) & Upload Cloud
            String fileUuid = UUID.randomUUID().toString(); 
            Map<String, String> cloudData = cloudinaryService.uploadFileSecure(avatarFile, fileUuid);
            
            newImageUrl = cloudData.get("url");
            newPublicId = cloudData.get("public_id");
        }

        // Save DB & Kiểm soát Transaction 
        User userToUpdate = findByEmail(email);
        String oldPublicId = userToUpdate.getAvatarPublicId(); 
        
        userMapper.updateEntityFromDTO(dto, userToUpdate);

        if (newImageUrl != null && newPublicId != null) {
            userToUpdate.setAvatarUrl(newImageUrl); 
            userToUpdate.setAvatarPublicId(newPublicId); 
        }

        try {
            userRepository.save(userToUpdate);

            final String finalOldPublicId = oldPublicId;
            final String finalNewPublicId = newPublicId;
            final boolean hasNewFile = (avatarFile != null && !avatarFile.isEmpty());

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_COMMITTED) {
                        if (hasNewFile && finalOldPublicId != null) 
                            cloudinaryService.deleteFile(finalOldPublicId);
                    } else if (status == STATUS_ROLLED_BACK) {
                        if (finalNewPublicId != null) {
                            log.warn("DB Rollback! Tiến hành xóa ảnh mồ côi: {}", finalNewPublicId);
                            cloudinaryService.deleteFile(finalNewPublicId);
                        }
                    }
                }
            });

        } catch (Exception e) {
            if (newPublicId != null) {
                log.warn("Lỗi lưu DB khẩn cấp! Xóa ảnh mồ côi: {}", newPublicId);
                cloudinaryService.deleteFile(newPublicId);
            }
            throw e;
        }
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