package com.example.ecommerce.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product name is required") @Size(max = 255, message = "Product name must not exceed 255 characters") String name,

        String description,

        @NotNull(message = "Price is required") @Min(value = 0, message = "Price must be greater than or equal to 0") BigDecimal price,

        @NotNull(message = "Stock is required") @Min(value = 0, message = "Stock must be greater than or equal to 0") Integer stock,

        Long categoryId) {

    /** Returns an empty request — all fields mean "no change". */
    public static ProductRequest empty() {
        return new ProductRequest(null, null, null, null, null);
    }

    public ProductRequest withName(String name) {
        return new ProductRequest(name, description, price, stock, categoryId);
    }

    public ProductRequest withDescription(String description) {
        return new ProductRequest(name, description, price, stock, categoryId);
    }

    public ProductRequest withPrice(BigDecimal price) {
        return new ProductRequest(name, description, price, stock, categoryId);
    }

    public ProductRequest withStock(Integer stock) {
        return new ProductRequest(name, description, price, stock, categoryId);
    }

    public ProductRequest withCategoryId(Long categoryId) {
        return new ProductRequest(name, description, price, stock, categoryId);
    }
}