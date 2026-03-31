package com.example.English.teaching.center.service.auth;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
    @Value("${app.jwt.refreshExpirationMs}") //7 ngày
    private Long refreshExpirationMs;

    private final int MAX_ACTIVE_DEVICES = 3;

    private final RefreshTokenRepository refreshTokenRepository;
    
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByUserOrderByExpiryDateAsc(user);

        while(activeTokens.size() >= MAX_ACTIVE_DEVICES){
            RefreshToken oldestToken = activeTokens.get(0);
            refreshTokenRepository.delete(oldestToken);
            activeTokens.remove(0);
        }
 
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plus(refreshExpirationMs, ChronoUnit.MILLIS));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public String processRefreshToken(String refreshTokenString, JwtUtils jwtUtils) {
        RefreshToken rt = refreshTokenRepository.findByToken(refreshTokenString)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy Refresh Token"));

        if(rt.getExpiryDate().isBefore(LocalDateTime.now())){
            refreshTokenRepository.delete(rt);
            throw new RuntimeException("Refresh Token đã hết hạn.");
        }

        return jwtUtils.generateTokenFromUsername(rt.getUser().getEmail());
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(LocalDateTime.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}