package com.example.demo4.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Generic paginated response wrapper.
 *
 * @param <T> the type of items in the page
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record PagedResponse<T>(
                List<T> content,
                PaginationResponse pagination) {
}