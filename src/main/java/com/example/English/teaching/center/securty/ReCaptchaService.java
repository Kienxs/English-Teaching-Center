package com.example.English.teaching.center.securty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class ReCaptchaService {

    @Value("${recaptcha.secret-key}")
    private String secretKey;

    private static final String RECAPTCHA_VERIFY_URL = 
        "https://www.google.com/recaptcha/api/siteverify";

    /**
     * Xác minh reCAPTCHA token
     */
    public boolean verify(String captchaResponse) {
        if (captchaResponse == null || captchaResponse.isEmpty()) {
            return false;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", secretKey);
            params.add("response", captchaResponse);

            String response = restTemplate.postForObject(
                RECAPTCHA_VERIFY_URL, 
                params, 
                String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response);
            
            return jsonNode.get("success").asBoolean();
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}