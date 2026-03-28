package com.example.English.teaching.center.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.English.teaching.center.entity.RefreshToken;
import com.example.English.teaching.center.service.auth.RefreshTokenService;
import com.example.English.teaching.center.utils.JwtUtils;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;

    public AuthApiController(RefreshTokenService refreshTokenService, JwtUtils jwtUtils) {
        this.refreshTokenService = refreshTokenService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String requestRefreshToken = request.get("refreshToken");

        if (requestRefreshToken == null || requestRefreshToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Refresh Token không được để trống!");
        }

        try {
            // 1. Tìm Refresh Token trong Database (nếu bị xóa ở đoạn while size >= 2 thì sẽ văng lỗi ở đây)
            return refreshTokenService.findByToken(requestRefreshToken)
                    // 2. Kiểm tra xem token này còn hạn không (nếu hết hạn thì hàm verifyExpiration sẽ xóa nó và ném lỗi)
                    .map(refreshTokenService::verifyExpiration)
                    // 3. Lấy thông tin User sở hữu token này
                    .map(RefreshToken::getUser)
                    // 4. Sinh Access Token mới cho User
                    .map(user -> {
                        String newAccessToken = jwtUtils.generateTokenFromUsername(user.getEmail());
                        
                        // Đóng gói trả về JSON cho frontend
                        Map<String, String> response = new HashMap<>();
                        response.put("accessToken", newAccessToken);
                        response.put("refreshToken", requestRefreshToken); // Trả về lại cái cũ để client tiếp tục dùng
                        
                        return ResponseEntity.ok(response);
                    })
                    .orElseThrow(() -> new RuntimeException("Refresh token không tồn tại hoặc đã bị đăng xuất ở thiết bị này!"));

        } catch (Exception e) {
            // Trả về lỗi 403 Forbidden hoặc 400 Bad Request để frontend api-client.js bắt được (!refreshRes.ok)
            return ResponseEntity.status(403).body(Map.of("message", e.getMessage()));
        }
    }
}