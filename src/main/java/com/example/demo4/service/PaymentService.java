package com.example.demo4.service;

import com.example.demo4.dto.response.OrderResponse;

public interface PaymentService {

    OrderResponse payForOrder(Long userId, Long orderId);
}

