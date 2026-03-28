package com.example.English.teaching.center.securty;

import java.util.Collections;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.example.English.teaching.center.entity.User;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.service.auth.AuthService;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final AuthService authService;

    public CustomAuthenticationProvider(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            User user = authService.authenticateOnly(email, password);

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
            );

            return new UsernamePasswordAuthenticationToken(user, null, authorities);
        } catch (RateLimitException | IllegalAccessException e) {
            throw new BadCredentialsException(e.getMessage()); 
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}