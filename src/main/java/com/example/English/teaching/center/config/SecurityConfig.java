package com.example.English.teaching.center.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.English.teaching.center.securty.CustomAuthenticationProvider;
import com.example.English.teaching.center.securty.CustomAuthenticationSuccessHandler;
import com.example.English.teaching.center.securty.JwtAuthenticationFilter;
import com.example.English.teaching.center.securty.ReCaptchaFilter;

@Configuration
public class SecurityConfig {

    private final CustomAuthenticationProvider customAuthProvider;
    private final ReCaptchaFilter reCaptchaFilter;
    private final CustomAuthenticationSuccessHandler customSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(@Lazy CustomAuthenticationProvider customAuthProvider, 
                          ReCaptchaFilter reCaptchaFilter,
                          CustomAuthenticationSuccessHandler customSuccessHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customAuthProvider = customAuthProvider;
        this.reCaptchaFilter = reCaptchaFilter;
        this.customSuccessHandler = customSuccessHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/landing", "/login", "/register", "/verify", "/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "TECHNICAL")
                .requestMatchers("/teacher/**").hasRole("TEACHER")
                .requestMatchers("/user/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                .requestMatchers("/api/courses/**").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(customAuthProvider)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/process-login")
                .successHandler(customSuccessHandler) // Sử dụng Handler chuẩn của bạn
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/landing")
                // Xóa cookie khi đăng xuất để bảo mật hơn
                .deleteCookies("accessToken", "refreshToken", "JSESSIONID") 
                .permitAll()
            )
            // Thêm JWT Filter và ReCaptcha Filter vào đúng chuỗi
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(reCaptchaFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}