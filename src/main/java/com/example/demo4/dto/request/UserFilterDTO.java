package com.example.demo4.dto.request;

/**
 * Immutable filter criteria for user queries.
 * Role should be provided without the "ROLE_" prefix (e.g., "ADMIN", "USER").
 */
public record UserFilterDTO(
                String username,
                String email,
                String fullName,
                Boolean enabled,
                String role) {
    
    /** Returns an empty request — all fields mean "no change". */
    public static UserFilterDTO empty() {
        return new UserFilterDTO(null, null, null, null, null);
    }
}