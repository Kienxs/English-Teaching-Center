package com.example.English.teaching.center.dto.report;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class MonthlyRevenueResponse {
    String getRevenueMonth;
    Long getTotalEnrollments;
    BigDecimal getTotalRevenue;
}