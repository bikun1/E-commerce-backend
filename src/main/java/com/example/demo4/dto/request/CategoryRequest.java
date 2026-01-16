package com.example.demo4.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 255) String name,

        @Size(max = 500) String description) {

    public static CategoryRequest empty() {
        return new CategoryRequest(null, null);
    }

    public CategoryRequest withName(String name) {
        return new CategoryRequest(name, description);
    }

    public CategoryRequest withDescription(String description) {
        return new CategoryRequest(name, description);
    }
}