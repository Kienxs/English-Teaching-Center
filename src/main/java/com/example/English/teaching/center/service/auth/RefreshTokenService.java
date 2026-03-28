package com.example.English.teaching.center.service.auth;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.English.teaching.center.entity.RefreshToken;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.repository.RefreshTokenRepository;

import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {
    @Value("${ece.jwt.refreshExpirationMs:604800000}") //7 ngày
    private Long refreshExpirationMs;

    private final int MAX_ACTIVE_DEVICES = 2;

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
        refreshToken.setUser(user); // Gán thẳng object user vào đây! Cực kỳ nhẹ.
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }
        return token;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
}