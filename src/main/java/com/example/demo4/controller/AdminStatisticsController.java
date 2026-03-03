package com.example.demo4.controller;

import com.example.demo4.dto.response.AdminDashboardResponse;
import com.example.demo4.dto.response.ApiResponse;
import com.example.demo4.dto.response.RevenueByMonthResponse;
import com.example.demo4.dto.response.TopSellingProductResponse;
import com.example.demo4.service.AdminStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/statistics")
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Statistics", description = "Admin dashboard statistics APIs")
public class AdminStatisticsController {

    private final AdminStatisticsService adminStatisticsService;

    public AdminStatisticsController(AdminStatisticsService adminStatisticsService) {
        this.adminStatisticsService = adminStatisticsService;
    }

    @GetMapping("/revenue-by-month")
    @Operation(summary = "Get revenue by month", description = "Returns monthly revenue from paid/shipped/completed orders")
    public ResponseEntity<ApiResponse<List<RevenueByMonthResponse>>> getRevenueByMonth() {
        List<RevenueByMonthResponse> revenue = adminStatisticsService.getRevenueByMonth();
        return ResponseEntity.ok(ApiResponse.success("Revenue by month retrieved successfully", revenue));
    }

    @GetMapping("/top-selling-products")
    @Operation(summary = "Get top selling products", description = "Returns products ranked by total quantity sold")
    public ResponseEntity<ApiResponse<List<TopSellingProductResponse>>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<TopSellingProductResponse> topProducts = adminStatisticsService.getTopSellingProducts(limit);
        return ResponseEntity.ok(ApiResponse.success("Top selling products retrieved successfully", topProducts));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics", description = "Returns combined dashboard data: revenue by month and top selling products")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboard(
            @RequestParam(defaultValue = "10") int topProductsLimit) {
        AdminDashboardResponse dashboard = adminStatisticsService.getDashboardStatistics(topProductsLimit);
        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", dashboard));
    }
}
