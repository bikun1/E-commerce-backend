package com.example.ecommerce.backend.service;

import com.example.ecommerce.backend.dto.request.UpdateOrderStatusRequest;
import com.example.ecommerce.backend.dto.response.OrderResponse;
import com.example.ecommerce.backend.dto.response.PagedResponse;

public interface OrderService {

    OrderResponse createOrder(Long userId);

    PagedResponse<OrderResponse> getOrderHistory(Long userId, int page, int size);

    OrderResponse getOrderById(Long userId, Long orderId);

    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);
}
