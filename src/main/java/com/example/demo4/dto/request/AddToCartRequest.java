package com.example.demo4.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotNull(message = "Product ID is required") Long productId,

        @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity) {
    /** Default quantity to 1 if not provided. */
    public AddToCartRequest {
        if (quantity == null)
            quantity = 1;
    }

    /** Returns an empty request — all fields mean "no change". */
    public static AddToCartRequest empty() {
        return new AddToCartRequest(null, null);
    }

    public AddToCartRequest withQuantity(Integer quantity) {
        return new AddToCartRequest(productId, quantity);
    }

    public AddToCartRequest withProductId(Long productId) {
        return new AddToCartRequest(productId, quantity);
    }
}