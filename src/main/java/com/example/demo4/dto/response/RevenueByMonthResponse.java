package com.example.demo4.dto.response;

import java.math.BigDecimal;

public record RevenueByMonthResponse(
        int year,
        int month,
        String yearMonth,
        BigDecimal revenue) {
}