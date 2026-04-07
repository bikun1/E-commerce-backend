package com.example.ecommerce.backend.dto.response;

import com.example.ecommerce.backend.entity.Order;
import com.example.ecommerce.backend.entity.OrderStatus;
import com.example.ecommerce.backend.entity.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        BigDecimal totalPrice,
        OrderStatus status,
        PaymentStatus paymentStatus,
        String transactionId,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt,
        List<OrderItemResponse> items) {
    public static OrderResponse fromEntity(Order order) {
        if (order == null)
            return null;

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getTransactionId(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(OrderItemResponse::fromEntity)
                        .toList());
    }
}