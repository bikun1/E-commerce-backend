package com.example.ecommerce.backend.service.impl;

import com.example.ecommerce.backend.dto.request.ReviewRequest;
import com.example.ecommerce.backend.dto.response.ReviewResponse;
import com.example.ecommerce.backend.entity.Product;
import com.example.ecommerce.backend.entity.Review;
import com.example.ecommerce.backend.entity.User;
import com.example.ecommerce.backend.exception.ResourceNotFoundException;
import com.example.ecommerce.backend.repository.ProductRepository;
import com.example.ecommerce.backend.repository.ReviewRepository;
import com.example.ecommerce.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final Long USER_ID = 1L;
    private static final Long PRODUCT_ID = 1L;
    private static final Long UNKNOWN_ID = 999L;
    private static final int VALID_RATING = 5;
    private static final String VALID_COMMENT = "Excellent";

    // ── Mocks & SUT ───────────────────────────────────────────────────────────
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    // ── Shared fixtures ───────────────────────────────────────────────────────
    private Product knownProduct;
    private User knownUser;

    @BeforeEach
    void setUpFixtures() {
        knownProduct = buildProduct(PRODUCT_ID);
        knownUser = buildUser(USER_ID);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // submitReview()
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("submitReview()")
    class SubmitReview {

        /*
         * NOTE: Rating bound validation (null / <1 / >5) is intentionally NOT
         * tested here. The service delegates that responsibility to Bean
         * Validation (@Min/@Max on ReviewRequest); those constraints should be
         * covered by a separate @Valid / ConstraintViolation unit test on the
         * DTO, not by service-layer tests.
         */

        @Test
        @DisplayName("throws ResourceNotFoundException when product does not exist")
        void throwsWhenProductNotFound() {
            when(productRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.submitReview(USER_ID, UNKNOWN_ID, validRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product")
                    .hasMessageContaining(String.valueOf(UNKNOWN_ID));

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when user does not exist")
        void throwsWhenUserNotFound() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(knownProduct));
            when(userRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> reviewService.submitReview(UNKNOWN_ID, PRODUCT_ID, validRequest()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User")
                    .hasMessageContaining(String.valueOf(UNKNOWN_ID));

            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("creates a new review when the user has not reviewed this product before")
        void createsNewReviewWhenNoneExists() {
            ReviewRequest request = requestWith(5, "Brand new review");
            Review saved = buildReview(1L, knownUser, knownProduct, 5, "Brand new review");

            stubProductAndUserFound();
            when(reviewRepository.findByUserIdAndProductId(USER_ID, PRODUCT_ID))
                    .thenReturn(Optional.empty());
            when(reviewRepository.save(any(Review.class))).thenReturn(saved);
            stubAverageRating(5.0);

            ReviewResponse result = reviewService.submitReview(USER_ID, PRODUCT_ID, request);

            assertThat(result).isNotNull();
            assertThat(result.rating()).isEqualTo(5);
            verify(reviewRepository)
                    .save(argThat(r -> r.getRating() == 5 && "Brand new review".equals(r.getComment())));
        }

        @Test
        @DisplayName("updates rating and comment when the user already reviewed this product")
        void updatesExistingReview() {
            ReviewRequest request = requestWith(4, "Updated comment");
            Review existingReview = buildReview(1L, knownUser, knownProduct, 3, "Old comment");

            stubProductAndUserFound();
            when(reviewRepository.findByUserIdAndProductId(USER_ID, PRODUCT_ID))
                    .thenReturn(Optional.of(existingReview));
            when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));
            stubAverageRating(4.0);

            ReviewResponse result = reviewService.submitReview(USER_ID, PRODUCT_ID, request);

            assertThat(result).isNotNull();
            assertThat(existingReview.getRating()).isEqualTo(4);
            assertThat(existingReview.getComment()).isEqualTo("Updated comment");
        }

        // ── Stubs ─────────────────────────────────────────────────────────────
        private void stubAverageRating(double avg) {
            when(reviewRepository.getAverageRatingForProduct(PRODUCT_ID)).thenReturn(avg);
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // getReviewsForProduct()
    // ══════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getReviewsForProduct()")
    class GetReviewsForProduct {

        @Test
        @DisplayName("throws ResourceNotFoundException when product does not exist")
        void throwsWhenProductNotFound() {
            when(productRepository.existsById(UNKNOWN_ID)).thenReturn(false);

            assertThatThrownBy(() -> reviewService.getReviewsForProduct(UNKNOWN_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product")
                    .hasMessageContaining(String.valueOf(UNKNOWN_ID));
        }

        @Test
        @DisplayName("returns mapped ReviewResponse list for a known product")
        void returnsMappedReviewsForKnownProduct() {
            Review review = buildReview(1L, knownUser, knownProduct, VALID_RATING, VALID_COMMENT);

            when(productRepository.existsById(PRODUCT_ID)).thenReturn(true);
            when(reviewRepository.findByProductId(PRODUCT_ID)).thenReturn(List.of(review));

            List<ReviewResponse> result = reviewService.getReviewsForProduct(PRODUCT_ID);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).rating()).isEqualTo(VALID_RATING);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Shared stubs
    // ══════════════════════════════════════════════════════════════════════════
    private void stubProductAndUserFound() {
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(knownProduct));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(knownUser));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Request factories
    // ══════════════════════════════════════════════════════════════════════════

    /** Returns a request with the default valid rating and comment. */
    private ReviewRequest validRequest() {
        return requestWith(VALID_RATING, VALID_COMMENT);
    }

    private ReviewRequest requestWith(int rating, String comment) {
        ReviewRequest req = ReviewRequest.empty()
                .withRating(rating)
                .withComment(comment);
        return req;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Entity builders
    // ══════════════════════════════════════════════════════════════════════════
    private Product buildProduct(Long id) {
        Product p = new Product();
        p.setId(id);
        return p;
    }

    private User buildUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setUsername("user");
        u.setEmail("user@test.com");
        return u;
    }

    private Review buildReview(Long id, User user, Product product, int rating, String comment) {
        Review r = new Review();
        r.setId(id);
        r.setUser(user);
        r.setProduct(product);
        r.setRating(rating);
        r.setComment(comment);
        return r;
    }
}