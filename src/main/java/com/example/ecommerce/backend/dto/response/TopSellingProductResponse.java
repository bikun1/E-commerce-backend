package com.example.ecommerce.backend.dto.response;

public record TopSellingProductResponse(
                Long productId,
                String productName,
                long totalQuantitySold) {
}