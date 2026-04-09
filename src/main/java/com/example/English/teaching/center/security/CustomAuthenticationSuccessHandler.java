package com.example.English.teaching.center.security;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.English.teaching.center.entity.RefreshToken;
import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.service.auth.RefreshTokenService;
import com.example.English.teaching.center.utils.JwtUtils;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public CustomAuthenticationSuccessHandler(JwtUtils jwtUtils, 
                                              RefreshTokenService refreshTokenService) {
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    // Hàm tiện ích lấy Cookie
    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        User user = (User) authentication.getPrincipal(); 

        //  1. Xử lý Device Fingerprint 
        String deviceId = getCookieValue(request, "deviceId");
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            Cookie deviceCookie = new Cookie("deviceId", deviceId);
            deviceCookie.setHttpOnly(true);
            deviceCookie.setPath("/");
            deviceCookie.setMaxAge(365 * 24 * 60 * 60); 
            response.addCookie(deviceCookie);
        }

        //  2. Create Access Token 
        String accessToken = jwtUtils.generateAccessToken(user);

        //  3. Create Refresh Token 
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, deviceId);

        // 4. Create HttpOnly Cookie for Access Token
        Cookie atCookie = new Cookie("accessToken", accessToken);
        atCookie.setHttpOnly(true);
        atCookie.setPath("/");
        atCookie.setMaxAge(15 * 60); // 15m
        response.addCookie(atCookie);

        // 5. Create HttpOnly Cookie for Refresh Token
        Cookie rtCookie = new Cookie("refreshToken", refreshToken.getToken());
        rtCookie.setHttpOnly(true);
        rtCookie.setPath("/");
        rtCookie.setMaxAge(7 * 24 * 60 * 60); 
        response.addCookie(rtCookie);

        // 6. Redirect to the homepage
        String redirectUrl = "/user/home"; 
        for (var auth : authentication.getAuthorities()) {
            if (auth.getAuthority().equals("ROLE_ADMIN")) {
                redirectUrl = "/admin/dashboard";
                break;
            } else if (auth.getAuthority().equals("ROLE_TEACHER")) {
                redirectUrl = "/teacher/dashboard";
                break;
            }
        }
        response.sendRedirect(redirectUrl);
    }
}