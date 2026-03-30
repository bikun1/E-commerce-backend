package com.example.demo4.dto.request;

/**
 * Groups pagination and sorting parameters.
 * String constants are usable directly in @RequestParam(defaultValue = ...)
 * annotations.
 * The compact constructor validates bounds so bad input fails fast at the
 * controller layer.
 */
public record PaginationRequest(int page, int size, String sortBy, String sortDir) {

    // String type required — @RequestParam(defaultValue) only accepts String
    // constants
    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_SIZE = "10";
    public static final String DEFAULT_SORT_BY = "id";
    public static final String DEFAULT_SORT_DIR = "asc";

    private static final int MAX_PAGE_SIZE = 100;

    public PaginationRequest {
        if (page < 0)
            throw new IllegalArgumentException("Page index must not be negative");
        if (size < 1 || size > MAX_PAGE_SIZE)
            throw new IllegalArgumentException(
                    "Page size must be between 1 and %d".formatted(MAX_PAGE_SIZE));
        if (sortBy == null || sortBy.isBlank())
            throw new IllegalArgumentException("sortBy must not be blank");
        if (sortDir == null || sortDir.isBlank())
            throw new IllegalArgumentException("sortDir must not be blank");
    }
}