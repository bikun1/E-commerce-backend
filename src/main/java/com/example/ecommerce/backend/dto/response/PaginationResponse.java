package com.example.ecommerce.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record PaginationResponse(
        int page,
        int pageSize,
        int totalPages,
        long totalElements) {
}

