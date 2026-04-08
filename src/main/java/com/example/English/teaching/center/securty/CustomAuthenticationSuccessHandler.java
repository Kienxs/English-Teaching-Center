package com.example.English.teaching.center.securty;

import java.io.IOException;

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
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler{
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public CustomAuthenticationSuccessHandler(JwtUtils jwtUtils, 
                                            RefreshTokenService refreshTokenService) {
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                            HttpServletResponse response,
                            Authentication authentication) throws IOException, ServletException {

        User user = (User) authentication.getPrincipal(); 
        String email = user.getEmail();

        // 1. Create Access Token
        String accessToken = jwtUtils.generateTokenFromUsername(email);

        // 2. Create Refresh Token and Save
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // 3. Create HttpOnly Cookie for Access Token
        Cookie atCookie = new Cookie("accessToken", accessToken);
        atCookie.setHttpOnly(true);
        atCookie.setPath("/");
        atCookie.setMaxAge(15*60); // 15m
        response.addCookie(atCookie);

        // 4. Create HttpOnly Cookie for Refresh Token
        Cookie rtCookie = new Cookie("refreshToken", refreshToken.getToken());
        rtCookie.setHttpOnly(true);
        rtCookie.setPath("/");
        rtCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(rtCookie);

        // 5. Redirect to the homepage
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
