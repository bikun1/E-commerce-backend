package com.example.ecommerce.backend.controller;

import com.example.ecommerce.backend.dto.request.ReviewRequest;
import com.example.ecommerce.backend.dto.response.ApiResponse;
import com.example.ecommerce.backend.dto.response.ReviewResponse;
import com.example.ecommerce.backend.security.UserDetailsImpl;
import com.example.ecommerce.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Reviews", description = "Product review APIs")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit review", description = "Submits a review for a purchased product")
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @AuthenticationPrincipal UserDetailsImpl principal,
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request) {

        ReviewResponse review = reviewService.submitReview(principal.getId(), productId, request);
        return ResponseEntity.ok(ApiResponse.success("Review submitted successfully", review));
    }

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Get product reviews", description = "Returns all reviews for a specific product")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
            @PathVariable Long productId) {

        List<ReviewResponse> reviews = reviewService.getReviewsForProduct(productId);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved successfully", reviews));
    }
}