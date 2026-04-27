package com.example.English.teaching.center.controller.payment;

import com.example.English.teaching.center.dto.payment.SePayWebhookRequest;
import com.example.English.teaching.center.service.finance.WebhookService;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

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

    @Value("${sepay.transfer-prefix}")
    private String transferPrefix;

    @PostMapping("/webhook")
    public ResponseEntity<?> handleSePayWebhook(@RequestBody SePayWebhookRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            if(authHeader == null || !isValidToken(authHeader)){
                log.error("🚨 CẢNH BÁO: Sai Token Webhook! Khả năng có tấn công.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String txnRef = extractTxnRefSafely(request.getContent());
            
            if (txnRef != null) 
                webhookService.processSePayWebhook(txnRef, request.getTransferAmount());
            else 
                log.warn("⚠️ Giao dịch rác hoặc không hợp lệ (Không tìm thấy mã ECE): {}", request.getContent());

            return ResponseEntity.ok(Map.of("success", true));
        }catch(IllegalArgumentException | EntityNotFoundException e){
            log.warn("Lỗi nghiệp vụ Webhook: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("success", true, "ignored", true));
        } catch (Exception e) {
            log.error("❌ Lỗi nghiêm trọng khi xử lý Webhook", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean isValidToken(String authHeader){
        String token = authHeader.replaceFirst("(?i)^Bearer\\s+", "")
                                 .replaceFirst("(?i)^Apikey\\s+", "")
                                 .trim();
        return expectedToken.equals(token);
    }

    private String extractTxnRefSafely(String content) {
        if (content == null || content.isBlank()) return null;
        
        String regex = "\\b" + Pattern.quote(transferPrefix) + "\\d{13}[A-Z0-9]{6}\\b";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE); 
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) return matcher.group().toUpperCase(); 
        return null;
    }
}