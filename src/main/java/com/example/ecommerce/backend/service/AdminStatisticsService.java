package com.example.ecommerce.backend.service;

import com.example.ecommerce.backend.dto.response.AdminDashboardResponse;
import com.example.ecommerce.backend.dto.response.RevenueByMonthResponse;
import com.example.ecommerce.backend.dto.response.TopSellingProductResponse;

import java.util.List;

public interface AdminStatisticsService {

    List<RevenueByMonthResponse> getRevenueByMonth();

    List<TopSellingProductResponse> getTopSellingProducts(int limit);

    AdminDashboardResponse getDashboardStatistics(int topProductsLimit);
}
