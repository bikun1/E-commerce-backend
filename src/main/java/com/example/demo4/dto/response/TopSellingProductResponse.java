package com.example.demo4.dto.response;

public record TopSellingProductResponse(
        Long productId,
        String productName,
        long totalQuantitySold) {
}