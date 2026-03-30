package com.example.demo4.dto.response;

import java.util.List;

public record AdminDashboardResponse(
        List<RevenueByMonthResponse> revenueByMonth,
        List<TopSellingProductResponse> topSellingProducts) {
}