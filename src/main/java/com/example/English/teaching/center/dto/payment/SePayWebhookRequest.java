package com.example.English.teaching.center.dto.payment;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SePayWebhookRequest {
    private String id;
    private String gateway;
    private String transactionDate;
    private String accountNumber;
    private String code;
    private String content;
    private String transferType;
    private BigDecimal transferAmount;
    private BigDecimal accumulated;
    private String referenceCode;
    private String description;
}
