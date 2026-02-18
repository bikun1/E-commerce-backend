package com.example.demo4.dto.response;

import com.example.demo4.entity.Review;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long productId,
        Long userId,
        String username,
        Integer rating,
        String comment,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt) {
    public static ReviewResponse fromEntity(Review review) {
        if (review == null)
            return null;

        return new ReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                review.getUser().getId(),
                review.getUser().getUsername(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt());
    }
}