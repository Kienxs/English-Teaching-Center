package com.example.English.teaching.center.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfig {
    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dypxfew4x");
        config.put("api_key", "568249469316255");
        config.put("api_secret", "Q9_5PBxPoRXUDgX0KNW5nihgAdo");
        return new Cloudinary(config);
    }
}
