package com.example.demo4.service.impl;

import com.example.demo4.dto.response.AdminDashboardResponse;
import com.example.demo4.dto.response.RevenueByMonthResponse;
import com.example.demo4.dto.response.TopSellingProductResponse;
import com.example.demo4.repository.OrderItemRepository;
import com.example.demo4.repository.OrderRepository;
import com.example.demo4.service.AdminStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public List<RevenueByMonthResponse> getRevenueByMonth() {
        return orderRepository.findRevenueByMonth().stream()
                .map(this::toRevenueByMonthResponse)
                .toList();
    }

    @Override
    public List<TopSellingProductResponse> getTopSellingProducts(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }

        return orderItemRepository.findTopSellingProducts(limit).stream()
                .map(this::toTopSellingProductResponse)
                .toList();
    }

    @Override
    public AdminDashboardResponse getDashboardStatistics(int topProductsLimit) {
        if (topProductsLimit <= 0) {
            throw new IllegalArgumentException("topProductsLimit must be greater than 0");
        }
        return new AdminDashboardResponse(
                getRevenueByMonth(),
                getTopSellingProducts(topProductsLimit));
    }

    // ---------------------------------------------------------------- mappers

    private RevenueByMonthResponse toRevenueByMonthResponse(Object[] row) {
        int year = ((Number) row[0]).intValue();
        int month = ((Number) row[1]).intValue();
        BigDecimal revenue = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
        String yearMonth = "%d-%02d".formatted(year, month);
        return new RevenueByMonthResponse(year, month, yearMonth, revenue);
    }

    private TopSellingProductResponse toTopSellingProductResponse(Object[] row) {
        Long productId = ((Number) row[0]).longValue();
        String productName = (String) row[1] != null ? (String) row[1] : "Unknown";
        long totalQty = ((Number) row[2]).longValue();
        return new TopSellingProductResponse(productId, productName, totalQty);
    }
}