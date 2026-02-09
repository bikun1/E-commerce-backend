package com.example.ecommerce.backend.dto.request;

import com.example.ecommerce.backend.entity.OrderStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Status is required") OrderStatus status) {
}