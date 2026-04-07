package com.example.ecommerce.backend.event;

import com.example.ecommerce.backend.dto.response.OrderResponse;

public record OrderEmailEvent(
        String to,
        OrderResponse order,
        OrderEmailEventType type) {
}
