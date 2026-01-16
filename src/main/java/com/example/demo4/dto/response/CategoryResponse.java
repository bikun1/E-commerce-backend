package com.example.demo4.dto.response;

import com.example.demo4.entity.Category;

public record CategoryResponse(
        Long id,
        String name,
        String description) {
    public static CategoryResponse fromEntity(Category category) {
        if (category == null)
            return null;

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription());
    }
}