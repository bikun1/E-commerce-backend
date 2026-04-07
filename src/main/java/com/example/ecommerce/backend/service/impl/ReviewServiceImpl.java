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
import com.example.ecommerce.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponse submitReview(Long userId, Long productId, ReviewRequest request) {
        // Rating bounds already enforced by @Min/@Max on ReviewRequest — no manual
        // check needed
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Upsert: update existing review or create new one
        Review review = reviewRepository.findByUserIdAndProductId(userId, productId)
                .orElseGet(Review::new);

        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.rating());
        review.setComment(request.comment());

        Review saved = reviewRepository.save(review);

        // Recalculate average rating on the product after review change
        product.setRating(reviewRepository.getAverageRatingForProduct(productId));
        productRepository.save(product);

        return ReviewResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }

        return reviewRepository.findByProductId(productId).stream()
                .map(ReviewResponse::fromEntity)
                .toList();
    }
}