package com.example.ecommerce.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Partial update — all fields are optional (null = no change).
 */
public record UpdateProductRequest(
        @Size(max = 255, message = "Product name must not exceed 255 characters") String name,

        String description,

        @DecimalMin(value = "0.0", inclusive = true, message = "Price must be greater than or equal to 0") BigDecimal price,

        @Min(value = 0, message = "Stock must be greater than or equal to 0") Integer stock,

        Long categoryId) {
    /** Returns an empty request — all fields mean "no change". */
    public static UpdateProductRequest empty() {
        return new UpdateProductRequest(null, null, null, null, null);
    }

    public UpdateProductRequest withName(String name) {
        return new UpdateProductRequest(name, description, price, stock, categoryId);
    }

    public UpdateProductRequest withDescription(String description) {
        return new UpdateProductRequest(name, description, price, stock, categoryId);
    }

    public UpdateProductRequest withPrice(BigDecimal price) {
        return new UpdateProductRequest(name, description, price, stock, categoryId);
    }

    public UpdateProductRequest withStock(Integer stock) {
        return new UpdateProductRequest(name, description, price, stock, categoryId);
    }

    public UpdateProductRequest withCategoryId(Long categoryId) {
        return new UpdateProductRequest(name, description, price, stock, categoryId);
    }
}