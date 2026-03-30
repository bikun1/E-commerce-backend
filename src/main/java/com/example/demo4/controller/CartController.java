package com.example.demo4.controller;

import com.example.demo4.dto.request.AddToCartRequest;
import com.example.demo4.dto.request.UpdateCartItemRequest;
import com.example.demo4.dto.response.ApiResponse;
import com.example.demo4.dto.response.CartResponse;
import com.example.demo4.security.UserDetailsImpl;
import com.example.demo4.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@PreAuthorize("isAuthenticated()")
@Tag(name = "Cart", description = "Shopping cart APIs")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get cart", description = "Returns current authenticated user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @AuthenticationPrincipal UserDetailsImpl principal) {

        CartResponse cart = cartService.getCart(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Cart fetched successfully", cart));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to cart", description = "Adds a product item to current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @Valid @RequestBody AddToCartRequest request) {

        CartResponse cart = cartService.addToCart(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PatchMapping("/items/{cartItemId}")
    @Operation(summary = "Update cart item quantity", description = "Updates quantity of an item in current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        CartResponse cart = cartService.updateQuantity(principal.getId(), cartItemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart updated successfully", cart));
    }

    @DeleteMapping("/items/{cartItemId}")
    @Operation(summary = "Remove cart item", description = "Removes an item from current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @PathVariable Long cartItemId) {

        CartResponse cart = cartService.removeItem(principal.getId(), cartItemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }
}