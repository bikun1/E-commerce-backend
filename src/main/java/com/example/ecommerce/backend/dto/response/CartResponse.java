package com.example.ecommerce.backend.dto.response;

import com.example.ecommerce.backend.entity.Cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long cartId,
        List<CartItemResponse> items,
        BigDecimal totalPrice) {
    /** Empty cart — used when the user has no cart yet. */
    public static CartResponse empty() {
        return new CartResponse(null, List.of(), BigDecimal.ZERO);
    }

    public static CartResponse fromEntity(Cart cart) {
        if (cart == null)
            return empty();

        List<CartItemResponse> items = cart.getItems().stream()
                .map(CartItemResponse::fromEntity)
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), items, total);
    }
}