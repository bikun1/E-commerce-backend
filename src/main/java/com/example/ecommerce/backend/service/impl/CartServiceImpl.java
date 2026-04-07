package com.example.ecommerce.backend.service.impl;

import com.example.ecommerce.backend.dto.request.AddToCartRequest;
import com.example.ecommerce.backend.dto.request.UpdateCartItemRequest;
import com.example.ecommerce.backend.dto.response.CartResponse;
import com.example.ecommerce.backend.entity.Cart;
import com.example.ecommerce.backend.entity.CartItem;
import com.example.ecommerce.backend.entity.Product;
import com.example.ecommerce.backend.entity.User;
import com.example.ecommerce.backend.exception.BadRequestException;
import com.example.ecommerce.backend.exception.ResourceNotFoundException;
import com.example.ecommerce.backend.repository.CartItemRepository;
import com.example.ecommerce.backend.repository.CartRepository;
import com.example.ecommerce.backend.repository.ProductRepository;
import com.example.ecommerce.backend.repository.UserRepository;
import com.example.ecommerce.backend.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.productId()));

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        // Calculate the total qty that will be in the cart after this operation
        int newQty = (item != null ? item.getQuantity() : 0) + request.quantity();
        validateStock(product, newQty);

        if (item != null) {
            item.setQuantity(newQty);
        } else {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.quantity());
        }
        cartItemRepository.save(item);

        return CartResponse.fromEntity(cart);
    }

    @Override
    @Transactional
    public CartResponse updateQuantity(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = findCartItemBelongingToCart(cartItemId, cart.getId());

        validateStock(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        return CartResponse.fromEntity(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long cartItemId) {
        // findByUserId only — we don't create a cart just to remove an item
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "userId", userId));

        CartItem item = findCartItemBelongingToCart(cartItemId, cart.getId());
        cart.getItems().remove(item);

        return CartResponse.fromEntity(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .map(CartResponse::fromEntity)
                .orElse(CartResponse.empty());
    }

    // ---------------------------------------------------------------- helpers

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    private CartItem findCartItemBelongingToCart(Long cartItemId, Long cartId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", cartItemId));
        if (!item.getCart().getId().equals(cartId)) {
            throw new BadRequestException("Cart item does not belong to your cart");
        }
        return item;
    }

    /** Throws if the product has insufficient stock for the requested quantity. */
    private void validateStock(Product product, int requestedQty) {
        if (product.getStock() != null && product.getStock() < requestedQty) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStock());
        }
    }
}