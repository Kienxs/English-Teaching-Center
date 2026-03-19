package com.example.English.teaching.center.securty;

import com.example.English.teaching.center.entity.RefreshToken;
import com.example.English.teaching.center.service.RefreshTokenService;
import com.example.English.teaching.center.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService, RefreshTokenService refreshTokenService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromCookie(request, "accessToken");
            
            // 1. Nếu có Access Token và còn hạn
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                authenticateUser(jwt, request);
            } 
            // 2. Nếu Access Token hết hạn, thử dùng Refresh Token
            else {
                String refreshToken = getJwtFromCookie(request, "refreshToken");
                if (refreshToken != null) {
                    Optional<RefreshToken> storedToken = refreshTokenService.findByToken(refreshToken);
                    
                    if (storedToken.isPresent()) {
                        RefreshToken rt = storedToken.get();
                        // Kiểm tra Refresh Token còn hạn trong DB không
                        if (rt.getExpiryDate().isAfter(java.time.Instant.now())) {
                            // Cấp Access Token mới
                            String email = rt.getUser().getEmail();
                            String newAccessToken = jwtUtils.generateTokenFromUsername(email);
                            
                            // Cập nhật lại Cookie mới cho trình duyệt
                            Cookie newAtCookie = new Cookie("accessToken", newAccessToken);
                            newAtCookie.setHttpOnly(true);
                            newAtCookie.setPath("/");
                            newAtCookie.setMaxAge(15 * 60); // 15 phút
                            response.addCookie(newAtCookie);
                            
                            // Đăng nhập vào hệ thống
                            authenticateUser(newAccessToken, request);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Không thể xác thực người dùng: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    // Hàm phụ để nạp thông tin User vào context của Spring Security
    private void authenticateUser(String token, HttpServletRequest request) {
        String username = jwtUtils.getUsernameFromJwtToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // Hàm phụ để lấy giá trị từ Cookie theo tên
    private String getJwtFromCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}