package com.example.English.teaching.center.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

public class NetworkUtils {
    public static String getClientIP(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    public static String getClientIPFromContext(){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes != null)
            return getClientIP(attributes.getRequest());

        return "UNKNOWN_IP";
    }
}