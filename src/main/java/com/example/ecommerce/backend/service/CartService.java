package com.example.ecommerce.backend.service;

import com.example.ecommerce.backend.dto.request.AddToCartRequest;
import com.example.ecommerce.backend.dto.request.UpdateCartItemRequest;
import com.example.ecommerce.backend.dto.response.CartResponse;

public interface CartService {

    CartResponse addToCart(Long userId, AddToCartRequest request);

    CartResponse updateQuantity(Long userId, Long cartItemId, UpdateCartItemRequest request);

    CartResponse removeItem(Long userId, Long cartItemId);

    CartResponse getCart(Long userId);
}
