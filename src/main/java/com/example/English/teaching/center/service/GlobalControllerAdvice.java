package com.example.English.teaching.center.service;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.English.teaching.center.dto.UserNavbarDTO;

@ControllerAdvice
public class GlobalControllerAdvice {
    private final UserService userService;

    public GlobalControllerAdvice(UserService userService){
        this.userService = userService;
    }

    @ModelAttribute("currentUser")
    public UserNavbarDTO addNavbarUserToModel(Principal principal){
        if(principal != null){
            try{
                return userService.getUserNavbarInfo(principal.getName());
            }catch(Exception e){
                return null;
            }
        }
        return null;
    }
}
