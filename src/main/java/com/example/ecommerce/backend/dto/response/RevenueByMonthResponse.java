package com.example.ecommerce.backend.dto.response;

import java.math.BigDecimal;

public record RevenueByMonthResponse(
                int year,
                int month,
                String yearMonth,
                BigDecimal revenue) {
}