package com.example.demo4.service;

import com.example.demo4.dto.response.OrderResponse;
import com.example.demo4.event.OrderEmailEventType;

public interface EmailService {

    void sendOrderEmail(String to, OrderResponse order, OrderEmailEventType type);
}

