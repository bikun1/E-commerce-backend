package com.example.ecommerce.backend.dto.response;

/**
 * Returned after a successful image upload.
 * Wraps the public URL rather than exposing a raw Map.
 */
public record ImageUploadResponse(String url) {
}