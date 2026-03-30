package com.example.demo4.service;

import com.example.demo4.dto.request.AddToCartRequest;
import com.example.demo4.dto.request.UpdateCartItemRequest;
import com.example.demo4.dto.response.CartResponse;

public interface CartService {

    CartResponse addToCart(Long userId, AddToCartRequest request);

    CartResponse updateQuantity(Long userId, Long cartItemId, UpdateCartItemRequest request);

    CartResponse removeItem(Long userId, Long cartItemId);

    CartResponse getCart(Long userId);
}
