package com.example.demo4.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Uniform envelope for all API responses.
 *
 * @param <T> the type of the response payload
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime timestamp) {
    /** Response with a payload. */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    /** Response with no payload (e.g., delete, restore). */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null, LocalDateTime.now());
    }
}