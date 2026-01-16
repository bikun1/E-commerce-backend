package com.example.ecommerce.backend.dto.request;

/**
 * Filter parameters for category queries.
 * All fields are optional — null means "no filter applied".
 */
public record CategoryFilterDTO(
        String name,
        String description) {
    /**
     * Convenience factory for controller usage — avoids positional constructor
     * confusion.
     */
    public static CategoryFilterDTO empty() {
        return new CategoryFilterDTO(null, null);
    }
}