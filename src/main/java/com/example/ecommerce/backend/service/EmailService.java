package com.example.ecommerce.backend.service;

import com.example.ecommerce.backend.dto.response.OrderResponse;
import com.example.ecommerce.backend.event.OrderEmailEventType;

public interface EmailService {

    void sendOrderEmail(String to, OrderResponse order, OrderEmailEventType type);
}
