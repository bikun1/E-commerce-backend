package com.example.demo4.service.impl;

import com.example.demo4.dto.request.UpdateOrderStatusRequest;
import com.example.demo4.dto.response.OrderResponse;
import com.example.demo4.dto.response.PagedResponse;
import com.example.demo4.dto.response.PaginationResponse;
import com.example.demo4.entity.*;
import com.example.demo4.event.OrderEmailEvent;
import com.example.demo4.event.OrderEmailEventType;
import com.example.demo4.exception.BadRequestException;
import com.example.demo4.exception.ResourceNotFoundException;
import com.example.demo4.repository.CartItemRepository;
import com.example.demo4.repository.CartRepository;
import com.example.demo4.repository.OrderRepository;
import com.example.demo4.repository.ProductRepository;
import com.example.demo4.repository.UserRepository;
import com.example.demo4.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

        private final OrderRepository orderRepository;
        private final CartRepository cartRepository;
        private final CartItemRepository cartItemRepository;
        private final ProductRepository productRepository;
        private final UserRepository userRepository;
        private final ApplicationEventPublisher eventPublisher;

        @Override
        @Transactional
        public OrderResponse createOrder(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

                Cart cart = cartRepository.findByUserId(userId)
                                .filter(c -> c.getItems() != null && !c.getItems().isEmpty())
                                .orElseThrow(() -> new BadRequestException(
                                                "Cart is empty. Add items before checkout."));

                Order order = buildOrder(user, cart);
                order = orderRepository.save(order);

                clearCart(cart);

                OrderResponse response = OrderResponse.fromEntity(order);
                eventPublisher.publishEvent(
                                new OrderEmailEvent(user.getEmail(), response, OrderEmailEventType.ORDER_CREATED));
                return response;
        }

        @Override
        @Transactional(readOnly = true)
        public PagedResponse<OrderResponse> getOrderHistory(Long userId, int page, int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
                Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);

                PaginationResponse pagination = new PaginationResponse(
                                orderPage.getNumber(),
                                orderPage.getSize(),
                                orderPage.getTotalPages(),
                                orderPage.getTotalElements());

                return new PagedResponse<>(
                                orderPage.getContent().stream().map(OrderResponse::fromEntity).toList(),
                                pagination);
        }

        @Override
        @Transactional(readOnly = true)
        public OrderResponse getOrderById(Long userId, Long orderId) {
                return orderRepository.findByIdAndUserId(orderId, userId)
                                .map(OrderResponse::fromEntity)
                                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        }

        @Override
        @Transactional
        public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

                if (request.status() == OrderStatus.CANCELLED
                                && order.getStatus() != OrderStatus.CANCELLED) {
                        restoreStockForOrder(order);
                }

                order.setStatus(request.status());
                return OrderResponse.fromEntity(orderRepository.save(order));
        }

        // ---------------------------------------------------------------- helpers

        /**
         * Builds an Order (with items) from a validated cart.
         * Validates stock and deducts it for each item — does NOT persist products
         * here;
         * all saves are batched via cascade or explicit saveAll after the loop.
         */
        private Order buildOrder(User user, Cart cart) {
                Order order = new Order();
                order.setUser(user);
                order.setStatus(OrderStatus.PENDING);

                BigDecimal totalPrice = BigDecimal.ZERO;
                List<Product> updatedProducts = new java.util.ArrayList<>();

                for (CartItem cartItem : cart.getItems()) {
                        Product product = cartItem.getProduct();
                        int qty = cartItem.getQuantity();

                        if (product.getStock() == null || product.getStock() < qty) {
                                throw new BadRequestException(
                                                "Insufficient stock for product '%s'. Available: %d"
                                                                .formatted(product.getName(),
                                                                                product.getStock() != null
                                                                                                ? product.getStock()
                                                                                                : 0));
                        }

                        BigDecimal unitPrice = product.getPrice() != null ? product.getPrice() : BigDecimal.ZERO;
                        totalPrice = totalPrice.add(unitPrice.multiply(BigDecimal.valueOf(qty)));

                        OrderItem orderItem = new OrderItem();
                        orderItem.setOrder(order);
                        orderItem.setProduct(product);
                        orderItem.setPrice(unitPrice);
                        orderItem.setQuantity(qty);
                        order.getItems().add(orderItem);

                        product.setStock(product.getStock() - qty);
                        updatedProducts.add(product);
                }

                order.setTotalPrice(totalPrice);
                // Batch update all deducted products in one round-trip instead of N saves
                productRepository.saveAll(updatedProducts);
                return order;
        }

        /** Clears the cart after a successful order — removes items then saves. */
        private void clearCart(Cart cart) {
                List<CartItem> itemsToRemove = List.copyOf(cart.getItems());
                cart.getItems().clear();
                cartItemRepository.deleteAll(itemsToRemove);
                cartRepository.save(cart);
        }

        /** Restores product stock for all items in a cancelled order. */
        private void restoreStockForOrder(Order order) {
                List<Product> updatedProducts = order.getItems().stream()
                                .filter(item -> item.getProduct().getStock() != null)
                                .peek(item -> item.getProduct()
                                                .setStock(item.getProduct().getStock() + item.getQuantity()))
                                .map(OrderItem::getProduct)
                                .toList();
                // Batch update instead of N saves in a loop
                productRepository.saveAll(updatedProducts);
        }
}