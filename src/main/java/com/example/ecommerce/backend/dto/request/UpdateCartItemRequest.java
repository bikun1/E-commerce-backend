package com.example.ecommerce.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateCartItemRequest(
                @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity) {

        /** Returns an empty request — all fields mean "no change". */
        public static UpdateCartItemRequest empty() {
                return new UpdateCartItemRequest(null);
        }

        public UpdateCartItemRequest withQuantity(Integer quantity) {
                return new UpdateCartItemRequest(quantity);
        }
}