package com.example.demo4.service;

import com.example.demo4.dto.request.ReviewRequest;
import com.example.demo4.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    ReviewResponse submitReview(Long userId, Long productId, ReviewRequest request);

    List<ReviewResponse> getReviewsForProduct(Long productId);
}

