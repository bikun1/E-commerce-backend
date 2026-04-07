package com.example.ecommerce.backend.service.impl;

import com.example.ecommerce.backend.dto.response.OrderResponse;
import com.example.ecommerce.backend.entity.Order;
import com.example.ecommerce.backend.entity.OrderStatus;
import com.example.ecommerce.backend.entity.PaymentStatus;
import com.example.ecommerce.backend.event.OrderEmailEvent;
import com.example.ecommerce.backend.event.OrderEmailEventType;
import com.example.ecommerce.backend.exception.BadRequestException;
import com.example.ecommerce.backend.exception.ResourceNotFoundException;
import com.example.ecommerce.backend.repository.OrderRepository;
import com.example.ecommerce.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
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
        log.info("Publishing ORDER_PAID email event for order {} to {}", response.id(), order.getUser().getEmail());
        eventPublisher.publishEvent(
                new OrderEmailEvent(order.getUser().getEmail(), response, OrderEmailEventType.ORDER_PAID));
        return response;
    }
}