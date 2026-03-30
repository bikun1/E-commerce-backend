package com.example.demo4.dto.response;

import com.example.demo4.entity.CartItem;
import com.example.demo4.entity.Product;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal) {
    public static CartItemResponse fromEntity(CartItem item) {
        Product product = item.getProduct();
        BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                unitPrice,
                item.getQuantity(),
                subtotal);
    }
}