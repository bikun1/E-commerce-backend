package com.example.demo4.service;

import com.example.demo4.dto.request.UpdateOrderStatusRequest;
import com.example.demo4.dto.response.OrderResponse;
import com.example.demo4.dto.response.PagedResponse;

public interface OrderService {

    OrderResponse createOrder(Long userId);

    PagedResponse<OrderResponse> getOrderHistory(Long userId, int page, int size);

    OrderResponse getOrderById(Long userId, Long orderId);

    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);
}
