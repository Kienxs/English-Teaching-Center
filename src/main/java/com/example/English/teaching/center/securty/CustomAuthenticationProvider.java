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

import com.example.English.teaching.center.dto.UserLoginResponseDTO;
import com.example.English.teaching.center.exception.RateLimitException;
import com.example.English.teaching.center.service.UserService;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserService userService;

    public CustomAuthenticationProvider(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        try {
            UserLoginResponseDTO loginDTO = userService.login(email, password);

            List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + loginDTO.getRole())
            );

            return new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), null, authorities);
        } catch (RateLimitException e) {
            throw new BadCredentialsException(e.getMessage()); 
        } catch (IllegalAccessException e) {
            throw new BadCredentialsException(e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
