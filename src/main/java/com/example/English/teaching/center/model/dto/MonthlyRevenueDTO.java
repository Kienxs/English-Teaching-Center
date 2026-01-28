package com.example.English.teaching.center.model.dto;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class MonthlyRevenueDTO {
    String getRevenueMonth;
    Long getTotalEnrollments;
    BigDecimal getTotalRevenue;
}
