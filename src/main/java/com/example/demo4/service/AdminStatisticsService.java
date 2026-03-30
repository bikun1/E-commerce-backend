package com.example.demo4.service;

import com.example.demo4.dto.response.AdminDashboardResponse;
import com.example.demo4.dto.response.RevenueByMonthResponse;
import com.example.demo4.dto.response.TopSellingProductResponse;

import java.util.List;

public interface AdminStatisticsService {

    List<RevenueByMonthResponse> getRevenueByMonth();

    List<TopSellingProductResponse> getTopSellingProducts(int limit);

    AdminDashboardResponse getDashboardStatistics(int topProductsLimit);
}
