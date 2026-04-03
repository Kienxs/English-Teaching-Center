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
                    if (jwtUtils.validateJwtToken(accessToken)) {
                        authenticateUser(accessToken, request);
                        isAccessTokenValid = true; 
                    }
                } catch (Exception e) {
                    logger.warn("Access Token không hợp lệ hoặc đã hết hạn. Đang thử dùng Refresh Token...");
                }
            }

            if (!isAccessTokenValid) {
                String refreshToken = getJwtFromCookie(request, "refreshToken");
                if (refreshToken != null) 
                    handleRefreshToken(refreshToken, request, response);
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
        try{
            String newAccessToken = refreshTokenService.processRefreshToken(refreshToken, jwtUtils);
            
            Cookie newAtCookie = new Cookie("accessToken", newAccessToken);
            newAtCookie.setHttpOnly(true);
            newAtCookie.setPath("/");
            newAtCookie.setMaxAge(15 * 60);
            response.addCookie(newAtCookie);

            authenticateUser(newAccessToken, request);
            logger.info("Đã gia hạn thành công Access Token!");
        } catch (Exception e) {
            logger.warn("Refresh Token không hợp lệ hoặc đã hết hạn: " + e.getMessage());
            clearCookies(response);
        }
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
        setCookie(response, "accessToken", "", 0);
        setCookie(response, "refreshToken", "", 0);
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