package com.example.demo4.service.impl;

import com.example.demo4.dto.response.OrderResponse;
import com.example.demo4.entity.Order;
import com.example.demo4.entity.OrderStatus;
import com.example.demo4.entity.PaymentStatus;
import com.example.demo4.event.OrderEmailEvent;
import com.example.demo4.event.OrderEmailEventType;
import com.example.demo4.exception.BadRequestException;
import com.example.demo4.exception.ResourceNotFoundException;
import com.example.demo4.repository.OrderRepository;
import com.example.demo4.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderResponse payForOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only pending orders can be paid.");
        }
        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException("Order has already been paid.");
        }

        // Fake payment gateway: always succeeds and generates a transaction id
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setTransactionId(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.PAID);

        OrderResponse response = OrderResponse.fromEntity(orderRepository.save(order));
        eventPublisher.publishEvent(
                new OrderEmailEvent(order.getUser().getEmail(), response, OrderEmailEventType.ORDER_PAID));
        return response;
    }
}