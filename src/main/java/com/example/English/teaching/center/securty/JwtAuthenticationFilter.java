package com.example.English.teaching.center.securty;

import com.example.English.teaching.center.entity.RefreshToken;
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
import java.time.Instant;

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
            String accessToken = getJwtFromCookie(request, "accessToken");

            // 1. Nếu Access Token hợp lệ -> Xác thực ngay
            if (accessToken != null && jwtUtils.validateJwtToken(accessToken)) {
                authenticateUser(accessToken, request);
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Nếu Access Token hỏng/hết hạn -> Thử cứu bằng Refresh Token
            String refreshToken = getJwtFromCookie(request, "refreshToken");
            if (refreshToken != null) {
                handleRefreshToken(refreshToken, request, response);
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực người dùng: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private void handleRefreshToken(String token, HttpServletRequest request, HttpServletResponse response) {
        refreshTokenService.findByToken(token).ifPresentOrElse(rt -> {
            // Kiểm tra Refresh Token còn hạn trong DB không
            if (rt.getExpiryDate().isAfter(Instant.now())) {
                // Cấp Access Token mới
                String email = rt.getUser().getEmail();
                String newAccessToken = jwtUtils.generateTokenFromUsername(email);

                Cookie newAtCookie = new Cookie("accessToken", newAccessToken);
                newAtCookie.setHttpOnly(true);
                newAtCookie.setPath("/");
                newAtCookie.setMaxAge(15 * 60); // 15 phút
                response.addCookie(newAtCookie);

                authenticateUser(newAccessToken, request);
            } else {
                clearCookies(response);
            }
        }, () -> {
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

    private void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }
}