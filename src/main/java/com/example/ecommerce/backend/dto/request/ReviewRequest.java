package com.example.ecommerce.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
                @NotNull @Min(1) @Max(5) Integer rating,

                String comment) {

        public static ReviewRequest empty() {
                return new ReviewRequest(null, null);
        }

        public ReviewRequest withRating(Integer rating) {
                return new ReviewRequest(rating, comment);
        }

        public ReviewRequest withComment(String comment) {
                return new ReviewRequest(rating, comment);
        }
}