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

        // 1. Lấy thông tin User-Agent
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) userAgent = "UNKNOWN";

        // 2. Xử lý UUID thô trong Cookie
        String rawDeviceId = getCookieValue(request, "deviceId");
        if (rawDeviceId == null) {
            rawDeviceId = UUID.randomUUID().toString();
            Cookie deviceCookie = new Cookie("deviceId", rawDeviceId);
            deviceCookie.setHttpOnly(true);
            deviceCookie.setPath("/");
            deviceCookie.setMaxAge(365 * 24 * 60 * 60); 
            deviceCookie.setSecure(true); 
            deviceCookie.setAttribute("SameSite", "Strict"); 
            response.addCookie(deviceCookie);
        }

        // 3. BĂM VÂN TAY (Hash) - Giống hệt bên Filter
        String secureDeviceId = org.springframework.util.DigestUtils.md5DigestAsHex((rawDeviceId + userAgent).getBytes());

        // 4. Create Access Token 
        String accessToken = jwtUtils.generateAccessToken(user);

        //  5. Create Refresh Token (Truyền vân tay đã mã hóa xuống DB)
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user, secureDeviceId);

        // 6. Create HttpOnly Cookie for Access Token
        Cookie atCookie = new Cookie("accessToken", accessToken);
        atCookie.setHttpOnly(true);
        atCookie.setPath("/");
        atCookie.setMaxAge(15 * 60); 
        atCookie.setSecure(true);
        atCookie.setAttribute("SameSite", "Strict"); 
        response.addCookie(atCookie);

        // 7. Create HttpOnly Cookie for Refresh Token
        Cookie rtCookie = new Cookie("refreshToken", refreshToken.getToken());
        rtCookie.setHttpOnly(true);
        rtCookie.setPath("/");
        rtCookie.setMaxAge(7 * 24 * 60 * 60); 
        rtCookie.setSecure(true);
        rtCookie.setAttribute("SameSite", "Strict"); 
        response.addCookie(rtCookie);

        // 8. Redirect
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