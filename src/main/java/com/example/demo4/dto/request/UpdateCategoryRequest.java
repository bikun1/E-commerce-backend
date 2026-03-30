package com.example.demo4.dto.request;

import jakarta.validation.constraints.Size;

/**
 * Partial update — all fields are optional (null = no change).
 */
public record UpdateCategoryRequest(
                @Size(max = 255, message = "Name must be less than 255 characters") String name,

                @Size(max = 500, message = "Description must be less than 500 characters") String description) {

        public static UpdateCategoryRequest empty() {
                return new UpdateCategoryRequest(null, null);
        }

        public UpdateCategoryRequest withName(String name) {
                return new UpdateCategoryRequest(name, description);
        }

        public UpdateCategoryRequest withDescription(String description) {
                return new UpdateCategoryRequest(name, description);
        }
}