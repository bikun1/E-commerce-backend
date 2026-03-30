package com.example.demo4.service.impl;

import com.example.demo4.dto.request.UpdateOrderStatusRequest;
import com.example.demo4.dto.response.OrderResponse;
import com.example.demo4.dto.response.PagedResponse;
import com.example.demo4.entity.*;
import com.example.demo4.event.OrderEmailEvent;
import com.example.demo4.exception.BadRequestException;
import com.example.demo4.exception.ResourceNotFoundException;
import com.example.demo4.repository.CartItemRepository;
import com.example.demo4.repository.CartRepository;
import com.example.demo4.repository.OrderRepository;
import com.example.demo4.repository.ProductRepository;
import com.example.demo4.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl")
class OrderServiceImplTest {

    // ── constants ────────────────────────────────────────────────────────────
    private static final long USER_ID = 1L;
    private static final long ORDER_ID = 1L;
    private static final long MISSING_ID = 999L;
    private static final long PRODUCT_ID = 1L;
    private static final String USERNAME = "user";
    private static final String EMAIL = "user@test.com";
    private static final BigDecimal UNIT_PRICE = new BigDecimal("10.00");
    private static final int STOCK = 20;

    // ── mocks ────────────────────────────────────────────────────────────────
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    // ── fixture builders ─────────────────────────────────────────────────────

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        u.setUsername(USERNAME);
        u.setEmail(EMAIL);
        return u;
    }

    private static Product product(long id, String name, BigDecimal price, int stock) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);
        return p;
    }

    private static Product defaultProduct() {
        return product(PRODUCT_ID, "Product A", UNIT_PRICE, STOCK);
    }

    private static Cart emptyCart(long cartId, User owner) {
        Cart c = new Cart();
        c.setId(cartId);
        c.setUser(owner);
        c.setItems(new ArrayList<>());
        return c;
    }

    private static Cart cartWithItem(User owner, Product p, int qty) {
        Cart cart = emptyCart(1L, owner);
        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(p);
        item.setQuantity(qty);
        cart.getItems().add(item);
        return cart;
    }

    private static Order pendingOrder(Product p, int qty) {
        Order order = new Order();
        order.setId(ORDER_ID);
        order.setUser(user(USER_ID));
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(p.getPrice().multiply(BigDecimal.valueOf(qty)));
        order.setItems(new ArrayList<>());

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(p);
        item.setPrice(p.getPrice());
        item.setQuantity(qty);
        order.getItems().add(item);
        return order;
    }

    private static Order savedOrder(User owner) {
        Order o = new Order();
        o.setId(ORDER_ID);
        o.setUser(owner);
        o.setStatus(OrderStatus.PENDING);
        o.setTotalPrice(new BigDecimal("30.00"));
        o.setItems(new ArrayList<>());
        return o;
    }

    private static UpdateOrderStatusRequest statusRequest(OrderStatus status) {
        return new UpdateOrderStatusRequest(status);
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("createOrder()")
    class CreateOrder {

        @Test
        @DisplayName("throws ResourceNotFoundException when user does not exist")
        void throwsWhenUserNotFound() {
            when(userRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(MISSING_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(MISSING_ID));

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws BadRequestException when user has no cart")
        void throwsWhenCartAbsent() {
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user(USER_ID)));
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(USER_ID))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cart is empty");
        }

        @Test
        @DisplayName("throws BadRequestException when cart exists but has no items")
        void throwsWhenCartEmpty() {
            User owner = user(USER_ID);
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(owner));
            when(cartRepository.findByUserId(USER_ID))
                    .thenReturn(Optional.of(emptyCart(1L, owner)));

            assertThatThrownBy(() -> orderService.createOrder(USER_ID))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Cart is empty");
        }

        @Test
        @DisplayName("throws BadRequestException when product stock is insufficient")
        void throwsWhenInsufficientStock() {
            User owner = user(USER_ID);
            Product low = product(PRODUCT_ID, "Product", UNIT_PRICE, 5);
            Cart cart = cartWithItem(owner, low, 10); // request 10, only 5 in stock

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(owner));
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

            assertThatThrownBy(() -> orderService.createOrder(USER_ID))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Insufficient stock")
                    .hasMessageContaining("Product");
        }

        @Test
        @DisplayName("creates order, deducts stock, clears cart, and publishes email event")
        void createsOrderSuccessfully() {
            User owner = user(USER_ID);
            Product p = defaultProduct(); // stock = 20
            Cart cart = cartWithItem(owner, p, 3); // order 3 → stock becomes 17

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(owner));
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
            when(orderRepository.save(any(Order.class))).thenReturn(savedOrder(owner));

            OrderResponse result = orderService.createOrder(USER_ID);

            assertThat(result).isNotNull();
            assertThat(p.getStock()).isEqualTo(17); // stock deducted
            assertThat(cart.getItems()).isEmpty(); // cart cleared
            verify(productRepository).saveAll(anyList()); // batch product update
            verify(cartItemRepository).deleteAll(anyList()); // cart items removed
            verify(eventPublisher).publishEvent(any(OrderEmailEvent.class));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateOrderStatus()")
    class UpdateOrderStatus {

        @Test
        @DisplayName("throws ResourceNotFoundException when order does not exist")
        void throwsWhenOrderNotFound() {
            when(orderRepository.findById(MISSING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.updateOrderStatus(MISSING_ID, statusRequest(OrderStatus.CANCELLED)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(MISSING_ID));
        }

        @Test
        @DisplayName("restores product stock when cancelling a non-cancelled order")
        void restoresStockOnCancel() {
            Product p = product(PRODUCT_ID, "P", UNIT_PRICE, 5);
            Order order = pendingOrder(p, 3); // stock 5, ordered 3 → restore → 8

            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            orderService.updateOrderStatus(ORDER_ID, statusRequest(OrderStatus.CANCELLED));

            assertThat(p.getStock()).isEqualTo(8);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            verify(productRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("skips stock restoration when order is already cancelled")
        void skipsRestoreWhenAlreadyCancelled() {
            Product p = product(PRODUCT_ID, "P", UNIT_PRICE, 5);
            Order order = pendingOrder(p, 3);
            order.setStatus(OrderStatus.CANCELLED);

            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            orderService.updateOrderStatus(ORDER_ID, statusRequest(OrderStatus.CANCELLED));

            assertThat(p.getStock()).isEqualTo(5); // unchanged
            verify(productRepository, never()).saveAll(any());
        }

        @Test
        @DisplayName("updates status without touching stock for non-cancel transitions")
        void updatesStatusWithoutRestoringStock() {
            Product p = product(PRODUCT_ID, "P", UNIT_PRICE, 5);
            Order order = pendingOrder(p, 2);

            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

            OrderResponse result = orderService.updateOrderStatus(ORDER_ID, statusRequest(OrderStatus.SHIPPED));

            assertThat(result.status()).isEqualTo(OrderStatus.SHIPPED);
            assertThat(p.getStock()).isEqualTo(5); // unchanged
            verify(productRepository, never()).saveAll(any());
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getOrderById()")
    class GetOrderById {

        @Test
        @DisplayName("returns OrderResponse when order belongs to the user")
        void returnsOrderResponse() {
            User owner = user(USER_ID);
            Order order = pendingOrder(defaultProduct(), 2);

            when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID))
                    .thenReturn(Optional.of(order));

            OrderResponse result = orderService.getOrderById(USER_ID, ORDER_ID);

            assertThat(result.id()).isEqualTo(ORDER_ID);
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when order does not belong to the user")
        void throwsWhenOrderNotFound() {
            when(orderRepository.findByIdAndUserId(MISSING_ID, USER_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(USER_ID, MISSING_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(String.valueOf(MISSING_ID));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getOrderHistory()")
    class GetOrderHistory {

        @Test
        @DisplayName("returns paged orders with correct content and pagination metadata")
        void returnsPagedHistory() {
            User owner = user(USER_ID);
            Order order = pendingOrder(defaultProduct(), 1);

            Page<Order> page = new PageImpl<>(
                    List.of(order),
                    PageRequest.of(0, 10),
                    1);

            when(orderRepository.findByUserId(any(), any(PageRequest.class))).thenReturn(page);

            PagedResponse<OrderResponse> result = orderService.getOrderHistory(USER_ID, 0, 10);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).status()).isEqualTo(OrderStatus.PENDING);
            assertThat(result.pagination().page()).isZero();
            assertThat(result.pagination().pageSize()).isEqualTo(10);
            assertThat(result.pagination().totalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("returns empty list when user has no orders")
        void returnsEmptyWhenNoOrders() {
            Page<Order> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(orderRepository.findByUserId(any(), any(PageRequest.class))).thenReturn(emptyPage);

            PagedResponse<OrderResponse> result = orderService.getOrderHistory(USER_ID, 0, 10);

            assertThat(result.content()).isEmpty();
            assertThat(result.pagination().totalElements()).isZero();
        }
    }
}