package com.example.English.teaching.center.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.English.teaching.center.securty.CustomAuthenticationProvider;
import com.example.English.teaching.center.securty.ReCaptchaFilter;


@Configuration
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, @Lazy CustomAuthenticationProvider customAuthProvider, ReCaptchaFilter reCaptchaFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/landing", "/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("TECHNICAL")
                .requestMatchers("/teacher/**").hasRole("TEACHER")
                .requestMatchers("/user/**").hasAnyRole("STUDENT")
                .anyRequest().authenticated()
            )
            .authenticationProvider(customAuthProvider)
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/process-login")
                .successHandler(successHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/landing")
                .permitAll()
            )


            .addFilterBefore(reCaptchaFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();
            String redirectUrl = "user/home";
            for (var auth : authorities) {
                if (auth.getAuthority().equals("ROLE_ADMIN")) {
                    redirectUrl = "/admin/dashboard";
                    break;
                }
                else if(auth.getAuthority().equals("ROLE_TEACHER")){
                    redirectUrl = "/teacher/course-management";
                    break;
                }
            }
            response.sendRedirect(redirectUrl);
        };
    }
}