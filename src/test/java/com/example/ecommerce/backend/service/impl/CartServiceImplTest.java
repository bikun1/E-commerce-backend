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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CartServiceImpl")
class CartServiceImplTest {

    // ── constants ────────────────────────────────────────────────────────────
    private static final long USER_ID = 1L;
    private static final long CART_ID = 1L;
    private static final long ITEM_ID = 5L;
    private static final long PRODUCT_ID = 10L;
    private static final int STOCK = 20;
    private static final BigDecimal UNIT_PRICE = new BigDecimal("25.00");

    // ── mocks ────────────────────────────────────────────────────────────────
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    // ── fixture builders ─────────────────────────────────────────────────────

    private static User user(long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private static Cart cart(long cartId, User owner) {
        Cart c = new Cart();
        c.setId(cartId);
        c.setUser(owner);
        c.setItems(new ArrayList<>());
        return c;
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
        return product(PRODUCT_ID, "Test Product", UNIT_PRICE, STOCK);
    }

    private static CartItem cartItem(long itemId, Cart ownerCart, Product p, int qty) {
        CartItem item = new CartItem();
        item.setId(itemId);
        item.setCart(ownerCart);
        item.setProduct(p);
        item.setQuantity(qty);
        return item;
    }

    private static AddToCartRequest addRequest(long productId, int qty) {
        return AddToCartRequest.empty().withProductId(productId).withQuantity(qty);
    }

    private static UpdateCartItemRequest updateRequest(int qty) {
        return UpdateCartItemRequest.empty().withQuantity(qty);
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getCart()")
    class GetCart {

        @Test
        @DisplayName("returns empty CartResponse when user has no cart")
        void returnsEmptyWhenNoCart() {
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            CartResponse result = cartService.getCart(USER_ID);

            assertThat(result.cartId()).isNull();
            assertThat(result.items()).isEmpty();
            assertThat(result.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("returns CartResponse with items and correct total when cart exists")
        void returnsCartWithItems() {
            User owner = user(USER_ID);
            Cart c = cart(CART_ID, owner);
            Product p = defaultProduct();

            // 2 × 25.00 = 50.00
            c.getItems().add(cartItem(ITEM_ID, c, p, 2));

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(c));

            CartResponse result = cartService.getCart(USER_ID);

            assertThat(result.cartId()).isEqualTo(CART_ID);
            assertThat(result.items()).hasSize(1);
            assertThat(result.totalPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("addToCart()")
    class AddToCart {

        @Test
        @DisplayName("creates a new cart and adds item when user has no existing cart")
        void createsCartAndAddsItem() {
            User owner = user(USER_ID);
            Cart c = cart(CART_ID, owner);
            Product p = defaultProduct();

            when(cartRepository.findByUserId(USER_ID))
                    .thenReturn(Optional.empty(), Optional.of(c));
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(owner));
            when(cartRepository.save(any(Cart.class))).thenReturn(c);
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(p));
            when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID))
                    .thenReturn(Optional.empty());
            when(cartItemRepository.save(any(CartItem.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            cartService.addToCart(USER_ID, addRequest(PRODUCT_ID, 2));

            verify(cartRepository).save(any(Cart.class));
            verify(cartItemRepository).save(argThat(item -> item.getProduct().getId().equals(PRODUCT_ID) &&
                    item.getQuantity() == 2));
        }

        @Test
        @DisplayName("increments quantity when the product is already in the cart")
        void incrementsQuantityWhenItemExists() {
            User owner = user(USER_ID);
            Cart c = cart(CART_ID, owner);
            Product p = defaultProduct();
            CartItem existing = cartItem(ITEM_ID, c, p, 2);
            c.getItems().add(existing);

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(c));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(p));
            when(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID))
                    .thenReturn(Optional.of(existing));
            when(cartItemRepository.save(any(CartItem.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            cartService.addToCart(USER_ID, addRequest(PRODUCT_ID, 3));

            // 2 existing + 3 added = 5
            assertThat(existing.getQuantity()).isEqualTo(5);
            verify(cartItemRepository).save(existing);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when product does not exist")
        void throwsWhenProductNotFound() {
            Cart c = cart(CART_ID, new User());
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(c));
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addToCart(USER_ID, addRequest(999L, 1)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");

            verify(cartItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws BadRequestException when requested quantity exceeds stock")
        void throwsWhenInsufficientStock() {
            Cart c = cart(CART_ID, new User());
            Product p = product(PRODUCT_ID, "Product", UNIT_PRICE, 5);

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(c));
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(p));

            assertThatThrownBy(() -> cartService.addToCart(USER_ID, addRequest(PRODUCT_ID, 10)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Insufficient stock");

            verify(cartItemRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user does not exist and cart must be created")
        void throwsWhenUserNotFound() {
            when(cartRepository.findByUserId(999L)).thenReturn(Optional.empty());
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.addToCart(999L, addRequest(PRODUCT_ID, 1)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateQuantity()")
    class UpdateQuantity {

        @Test
        @DisplayName("updates the cart item quantity when request is valid")
        void updatesQuantity() {
            Cart c = cart(CART_ID, user(USER_ID));
            Product p = defaultProduct();
            CartItem item = cartItem(ITEM_ID, c, p, 2);

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(c));
            when(cartItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));
            when(cartItemRepository.save(any(CartItem.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            cartService.updateQuantity(USER_ID, ITEM_ID, updateRequest(5));

            assertThat(item.getQuantity()).isEqualTo(5);
            verify(cartItemRepository).save(item);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when cart item does not exist")
        void throwsWhenCartItemNotFound() {
            Cart c = cart(CART_ID, new User());
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(c));
            when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.updateQuantity(USER_ID, 999L, updateRequest(1)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("CartItem not found");
        }

        @Test
        @DisplayName("throws BadRequestException when item belongs to a different cart")
        void throwsWhenItemBelongsToDifferentCart() {
            Cart userCart = cart(CART_ID, new User());
            Cart otherCart = cart(99L, new User());
            CartItem item = cartItem(ITEM_ID, otherCart,
                    product(PRODUCT_ID, "P", BigDecimal.ONE, 10), 1);

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userCart));
            when(cartItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

            assertThatThrownBy(() -> cartService.updateQuantity(USER_ID, ITEM_ID, updateRequest(2)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("does not belong to your cart");
        }

        @Test
        @DisplayName("throws BadRequestException when new quantity exceeds available stock")
        void throwsWhenInsufficientStock() {
            Cart c = cart(CART_ID, new User());
            Product p = product(PRODUCT_ID, "P", BigDecimal.ONE, 5);
            CartItem item = cartItem(ITEM_ID, c, p, 1);

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(c));
            when(cartItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

            assertThatThrownBy(() -> cartService.updateQuantity(USER_ID, ITEM_ID, updateRequest(10)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Insufficient stock");
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("removeItem()")
    class RemoveItem {

        @Test
        @DisplayName("removes item from cart items list")
        void removesItemFromCart() {
            Cart c = cart(CART_ID, user(USER_ID));
            Product p = defaultProduct();
            CartItem item = cartItem(ITEM_ID, c, p, 1);
            c.getItems().add(item);

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(c));
            when(cartItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

            cartService.removeItem(USER_ID, ITEM_ID);

            assertThat(c.getItems()).isEmpty();
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when cart item does not exist")
        void throwsWhenCartItemNotFound() {
            Cart c = cart(CART_ID, new User());
            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(c));
            when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartService.removeItem(USER_ID, 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("CartItem not found");
        }

        @Test
        @DisplayName("throws BadRequestException when item belongs to a different cart")
        void throwsWhenItemBelongsToDifferentCart() {
            Cart userCart = cart(CART_ID, new User());
            Cart otherCart = cart(99L, new User());
            CartItem item = cartItem(ITEM_ID, otherCart, new Product(), 1);

            when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(userCart));
            when(cartItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

            assertThatThrownBy(() -> cartService.removeItem(USER_ID, ITEM_ID))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("does not belong to your cart");
        }
    }
}