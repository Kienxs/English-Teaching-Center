package com.example.English.teaching.center.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.English.teaching.center.security.CustomAuthenticationProvider;
import com.example.English.teaching.center.security.CustomAuthenticationSuccessHandler;
import com.example.English.teaching.center.security.JwtAuthenticationFilter;
import com.example.English.teaching.center.security.ReCaptchaFilter;

import jakarta.servlet.http.HttpServletResponse;

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
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/landing", "/login", "/register", "/verify", "/forgot-password", "/reset-password", "/css/**", "/js/**", "/images/**", "/fonts/**", "/favicon.ico", "/error").permitAll()
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
                .successHandler(customSuccessHandler) 
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/landing")
                .deleteCookies("accessToken", "refreshToken", "JSESSIONID") 
                .permitAll()
            )

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(reCaptchaFilter, JwtAuthenticationFilter.class)

            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    String header = request.getHeader("Accept");
                    boolean isAjax = (header != null && header.contains("application/json"));

                    if (isAjax) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    } else {
                        response.sendRedirect("/login?kicked=true");
                    }
                })
            );

        return http.build();
    }
}