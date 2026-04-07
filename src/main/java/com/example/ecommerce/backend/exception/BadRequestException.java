package com.example.ecommerce.backend.exception;

/**
 * Thrown when the client sends invalid or unrecognizable input.
 * Maps to HTTP 400 in the global exception handler.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String field, String reason) {
        super("Invalid value for '%s': %s".formatted(field, reason));
    }
}