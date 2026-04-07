package com.example.ecommerce.backend.service;

import com.example.ecommerce.backend.dto.response.OrderResponse;

public interface PaymentService {

    OrderResponse payForOrder(Long userId, Long orderId);
}
