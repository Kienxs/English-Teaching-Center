package com.example.English.teaching.center.controller.payment;

import com.example.English.teaching.center.dto.payment.SePayWebhookRequest;
import com.example.English.teaching.center.service.finance.WebhookService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/sepay")
@RequiredArgsConstructor
@Slf4j
public class SePayWebhookController {
    private final WebhookService webhookService;

    @Value("${sepay.webhook-token}")
    private String expectedToken;

    private static final List<String> SEPAY_ALLOWED_IPS = Arrays.asList("1.2.3.4", "5.6.7.8", "127.0.0.1");

    @PostMapping("/webhook")
    public ResponseEntity<?> handleSePayWebhook(@RequestBody SePayWebhookRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest httpRequest) {

        try {
            // 1. Security Check: Validate IP
            String clientIp = getClientIp(httpRequest);

            if (!SEPAY_ALLOWED_IPS.contains(clientIp)) {
                log.error("🚨 Kẻ gian giả mạo IP gọi Webhook! IP: {}", clientIp);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // 2. Security Check: Validate Token
            if (authHeader == null || !isValidToken(authHeader)) {
                log.error("🚨 BẢO MẬT: Sai Token Webhook từ IP: {}", clientIp);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                     .body(Map.of("success", false, "message", "Invalid Token"));
            }

            log.info("🔔 Webhook Ting Ting: Nhận {} VNĐ", request.getTransferAmount());

            String txnRef = extractTxnRefSafely(request.getContent());
            
            if (txnRef != null) 
                webhookService.processSePayWebhook(txnRef, request.getTransferAmount());
            else 
                log.warn("⚠️ Giao dịch rác hoặc không hợp lệ (Không tìm thấy mã ECE): {}", request.getContent());

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("❌ Lỗi nghiêm trọng khi xử lý Webhook", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean isValidToken(String authHeader){
        String token = authHeader.replace("Bearer", "").replace("Apikey", "").trim();
        return expectedToken.equals(token);
    }

    private String extractTxnRefSafely(String content) {
        if (content == null || content.isBlank()) return null;
        
        Pattern pattern = Pattern.compile("ECE\\d+"); 
        Matcher matcher = pattern.matcher(content.toUpperCase());
        
        if (matcher.find()) 
            return matcher.group(); 

        return null;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}