package com.example.ecommerce.backend.service;

import com.example.ecommerce.backend.dto.request.ReviewRequest;
import com.example.ecommerce.backend.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse submitReview(Long userId, Long productId, ReviewRequest request);

    List<ReviewResponse> getReviewsForProduct(Long productId);
}
