package com.example.demo4.controller;

import com.example.demo4.dto.request.UpdateOrderStatusRequest;
import com.example.demo4.dto.response.ApiResponse;
import com.example.demo4.dto.response.OrderResponse;
import com.example.demo4.dto.response.PagedResponse;
import com.example.demo4.security.UserDetailsImpl;
import com.example.demo4.service.OrderService;
import com.example.demo4.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("isAuthenticated()")
@Tag(name = "Orders", description = "Order and payment APIs")
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Create order", description = "Creates a new order from current user's cart")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetailsImpl principal) {

        OrderResponse order = orderService.createOrder(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Order created successfully", order));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Pay for order", description = "Processes payment for a specific order")
    public ResponseEntity<ApiResponse<OrderResponse>> payForOrder(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @PathVariable Long id) {

        OrderResponse order = paymentService.payForOrder(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", order));
    }

    @GetMapping
    @Operation(summary = "Get order history", description = "Returns paginated order history of current user")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getOrderHistory(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<OrderResponse> history = orderService.getOrderHistory(principal.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success("Order history retrieved successfully", history));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by id", description = "Returns order details for current user")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @PathVariable Long id) {

        OrderResponse order = orderService.getOrderById(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update order status", description = "Updates status of an order (admin only)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderResponse order = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", order));
    }
}