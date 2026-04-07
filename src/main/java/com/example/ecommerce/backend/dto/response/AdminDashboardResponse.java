package com.example.ecommerce.backend.dto.response;

import java.util.List;

public record AdminDashboardResponse(
                List<RevenueByMonthResponse> revenueByMonth,
                List<TopSellingProductResponse> topSellingProducts) {
}