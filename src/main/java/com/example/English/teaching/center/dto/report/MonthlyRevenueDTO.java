package com.example.English.teaching.center.dto.report;

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
