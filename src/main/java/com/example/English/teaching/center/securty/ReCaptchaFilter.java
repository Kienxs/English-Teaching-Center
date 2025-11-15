package com.example.English.teaching.center.securty;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ReCaptchaFilter extends OncePerRequestFilter {

    private final ReCaptchaService reCaptchaService;

    public ReCaptchaFilter(ReCaptchaService reCaptchaService) {
        this.reCaptchaService = reCaptchaService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // Only check reCAPTCHA for login POST requests
        if ("/process-login".equals(request.getRequestURI()) 
                && "POST".equals(request.getMethod())) {
            
            String recaptchaResponse = request.getParameter("g-recaptcha-response");
            
            // SỬA: gọi đúng tên method verify() thay vì verifyRecaptcha()
            if (!reCaptchaService.verify(recaptchaResponse)) {
                response.sendRedirect("/login?error=true&captcha=true");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}