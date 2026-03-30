package com.example.demo4.event;

import com.example.demo4.dto.response.OrderResponse;

public record OrderEmailEvent(
        String to,
        OrderResponse order,
        OrderEmailEventType type
) {
}

