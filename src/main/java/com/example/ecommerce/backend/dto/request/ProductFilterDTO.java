package com.example.ecommerce.backend.dto.request;

import java.math.BigDecimal;

public record ProductFilterDTO(
        String name,
        Long categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Double minRating,
        Integer minStock) {

    public static ProductFilterDTO empty() {
        return new ProductFilterDTO(null, null, null, null, null, null);
    }
}