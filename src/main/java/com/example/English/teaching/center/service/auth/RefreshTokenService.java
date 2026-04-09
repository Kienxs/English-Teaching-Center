package com.example.English.teaching.center.service.auth;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.English.teaching.center.entity.RefreshToken;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.repository.RefreshTokenRepository;
import com.example.English.teaching.center.utils.JwtUtils;

import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {
    @Value("${app.jwt.refreshExpirationMs}") 
    private Long refreshExpirationMs;

    private final int MAX_ACTIVE_DEVICES = 3;
    private final RefreshTokenRepository refreshTokenRepository;
    
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user, String deviceId) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserAndIsRevokedFalseOrderByExpiryDateAsc(user);

        Optional<RefreshToken> existingDevice = activeTokens.stream()
                .filter(rt -> rt.getDeviceId().equals(deviceId))
                .findFirst();
 
        if(existingDevice.isPresent()){
            refreshTokenRepository.delete(existingDevice.get());
            activeTokens.remove(existingDevice.get());
        } else if(activeTokens.size() >= MAX_ACTIVE_DEVICES){
            RefreshToken oldestToken = activeTokens.get(0);
            refreshTokenRepository.delete(oldestToken);
        }
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setDeviceId(deviceId);
        refreshToken.setExpiryDate(LocalDateTime.now().plus(refreshExpirationMs, ChronoUnit.MILLIS));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public Map<String, String> processRefreshToken(String refreshTokenString, 
                                                JwtUtils jwtUtils,
                                                String deviceId) { 
        RefreshToken rt = refreshTokenRepository.findByToken(refreshTokenString)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Refresh Token"));

        User user = rt.getUser();
        
        if (rt.isRevoked()) {
            refreshTokenRepository.deleteAllByUser(user); 
            throw new SecurityException("Cảnh báo bảo mật: Phát hiện dấu hiệu đánh cắp Token! Toàn bộ phiên đăng nhập đã bị hủy.");
        }

        if(rt.getExpiryDate().isBefore(LocalDateTime.now()) || !rt.getDeviceId().equals(deviceId)){
            refreshTokenRepository.delete(rt);
            throw new RuntimeException("Refresh Token đã hết hạn hoặc thiết bị không hợp lệ. Vui lòng đăng nhập lại.");
        }

        rt.setRevoked(true);
        refreshTokenRepository.save(rt);

        String newAccessToken = jwtUtils.generateAccessToken(user);
        RefreshToken newRtEntity = createRefreshToken(user, deviceId);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        tokens.put("refreshToken", newRtEntity.getToken());
        return tokens;
    }

    public void deleteAllByUser(User user) {
        refreshTokenRepository.deleteAllByUser(user);
    }
}