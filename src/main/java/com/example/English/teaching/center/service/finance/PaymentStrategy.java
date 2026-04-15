package com.example.English.teaching.center.service.finance;

import java.math.BigDecimal;

public interface PaymentStrategy {
    String createPaymentUrl(String email, BigDecimal amount);
}
