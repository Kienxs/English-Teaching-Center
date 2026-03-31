package com.example.English.teaching.center.securty; // (Lưu ý: package name đang bị sai chính tả chữ 'security')

import com.example.English.teaching.center.service.auth.RefreshTokenService;
import com.example.English.teaching.center.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, 
                                   UserDetailsService userDetailsService, 
                                   RefreshTokenService refreshTokenService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String accessToken = getJwtFromCookie(request, "accessToken");
            boolean isAccessTokenValid = false;

            if (accessToken != null) {
                try {
                    // Kiểm tra xem Access Token còn hiệu lực không
                    if (jwtUtils.validateJwtToken(accessToken)) {
                        // Token còn sống -> Set user vào SecurityContext ngay lập tức
                        authenticateUser(accessToken, request);
                        isAccessTokenValid = true; 
                    }
                } catch (Exception e) {
                    logger.warn("Access Token không hợp lệ hoặc đã hết hạn. Đang thử dùng Refresh Token...");
                }
            }

            // 2. Nếu Access Token không có, hỏng hoặc hết hạn
            if (!isAccessTokenValid) {
                String refreshToken = getJwtFromCookie(request, "refreshToken");
                if (refreshToken != null) {
                    // Gọi hàm cứu tinh
                    handleRefreshToken(refreshToken, request, response);
                }
            }
            
        } catch (Exception e) {
            logger.error("Lỗi xác thực người dùng nghiêm trọng: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void handleRefreshToken(String refreshToken, 
                                    HttpServletRequest request, 
                                    HttpServletResponse response) {
        refreshTokenService.findByToken(refreshToken).ifPresentOrElse(rt -> {
            // Kiểm tra Refresh Token còn hạn trong Database không
            if (rt.getExpiryDate().isAfter(LocalDateTime.now())) {
                
                // 1. Cấp Access Token mới
                String email = rt.getUser().getEmail();
                String newAccessToken = jwtUtils.generateTokenFromUsername(email);

                // 2. Gắn token mới vào Cookie để gửi về trình duyệt
                Cookie newAtCookie = new Cookie("accessToken", newAccessToken);
                newAtCookie.setHttpOnly(true);
                newAtCookie.setPath("/");
                newAtCookie.setMaxAge(15 * 60); // 15 phút
                response.addCookie(newAtCookie);

                // 3.Cập nhật ngay Security Context cho Request HIỆN TẠI
                authenticateUser(newAccessToken, request);
                logger.info("Đã gia hạn thành công Access Token cho user: " + email);

            } else {
                logger.warn("Refresh Token đã hết hạn trong DB");
                clearCookies(response); 
            }
        }, () -> {
            logger.warn("Không tìm thấy Refresh Token trong DB");
            clearCookies(response); 
        });
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/css/") || path.startsWith("/js/") ||
               path.startsWith("/images/") || path.startsWith("/fonts/") ||
               path.equals("/favicon.ico");
    }

    private void authenticateUser(String token, HttpServletRequest request) {
        String username = jwtUtils.getUsernameFromJwtToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        // Đặt thông tin xác thực vào Context của Spring Security
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getJwtFromCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) return cookie.getValue();
            }
        }
        return null;
    }

    private void clearCookies(HttpServletResponse response) {
        setCookie(response, "accessToken", null, 0);
        setCookie(response, "refreshToken", null, 0);
    }

    private void setCookie(HttpServletResponse response, 
                           String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}