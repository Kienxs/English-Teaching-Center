package com.example.English.teaching.center.security;

import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.repository.UserRepository;
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
import java.util.Map;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository; 

    public JwtAuthenticationFilter(JwtUtils jwtUtils, 
                                   UserDetailsService userDetailsService, 
                                   RefreshTokenService refreshTokenService,
                                   UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String userAgent = request.getHeader("User-Agent");
            if (userAgent == null) userAgent = "UNKNOWN";

            String rawDeviceId = getJwtFromCookie(request, "deviceId");
            if (rawDeviceId == null) {
                rawDeviceId = UUID.randomUUID().toString();
                setCookie(response, "deviceId", rawDeviceId, 365 * 24 * 60 * 60); 
            }

            String secureDeviceId = org.springframework.util.DigestUtils.md5DigestAsHex((rawDeviceId + userAgent).getBytes());

            String accessToken = getJwtFromCookie(request, "accessToken");
            boolean isAccessTokenValid = false;

            if (accessToken != null) {
                try {
                    if (jwtUtils.validateJwtToken(accessToken)) {
                        String email = jwtUtils.getUsernameFromJwtToken(accessToken);
                        Integer jwtVersion = jwtUtils.getTokenVersionFromJwtToken(accessToken);
                        
                        User user = userRepository.findByEmail(email).orElse(null);

                        if (user != null && user.getTokenVersion().equals(jwtVersion)) {
                            authenticateUser(email, request);
                            isAccessTokenValid = true; 
                        } else {
                            logger.warn("Token Version không khớp! Buộc đăng xuất.");
                            clearCookies(response); 
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Access Token không hợp lệ hoặc đã hết hạn.");
                }
            }

            if (!isAccessTokenValid) {
                String refreshToken = getJwtFromCookie(request, "refreshToken");
                if (refreshToken != null) 
                    handleRefreshToken(refreshToken, secureDeviceId, request, response);
            }
            
        } catch (Exception e) {
            logger.error("Lỗi xác thực: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void handleRefreshToken(String refreshToken, String deviceId, 
                                    HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, String> newTokens = refreshTokenService.processRefreshToken(refreshToken, jwtUtils, deviceId);
            
            String newAccessToken = newTokens.get("accessToken");
            String newRefreshToken = newTokens.get("refreshToken");

            setCookie(response, "accessToken", newAccessToken, 15 * 60); // 15 phút
            setCookie(response, "refreshToken", newRefreshToken, 7 * 24 * 60 * 60); // 7 ngày

            String email = jwtUtils.getUsernameFromJwtToken(newAccessToken);
            authenticateUser(email, request);
            
            logger.info("Đã gia hạn thành công cặp Token mới cho user: " + email);
        } catch (Exception e) {
            logger.warn("Refresh Token thất bại: " + e.getMessage());
            clearCookies(response);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/css/") || path.startsWith("/js/") ||
               path.startsWith("/images/") || path.startsWith("/fonts/") ||
               path.equals("/favicon.ico") || path.startsWith("/auth/"); 
    }

    private void authenticateUser(String username, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
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
        setCookie(response, "accessToken", "", 0);
        setCookie(response, "refreshToken", "", 0);
    }

    private void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        
        cookie.setSecure(true); 
        cookie.setAttribute("SameSite", "Strict"); 
        
        response.addCookie(cookie);
    }
}