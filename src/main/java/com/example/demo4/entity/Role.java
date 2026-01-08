package com.example.demo4.entity;

import com.example.demo4.exception.BadRequestException;

public enum Role {
    ROLE_USER,
    ROLE_ADMIN,
    ROLE_MODERATOR;

    /**
     * Resolves a plain role label (e.g. "admin", "mod") to its enum constant.
     * Client sends labels without the "ROLE_" prefix.
     */
    public static Role fromString(String label) {
        if (label == null)
            throw new BadRequestException("role", "must not be null");

        return switch (label.toLowerCase()) {
            case "user" -> ROLE_USER;
            case "admin" -> ROLE_ADMIN;
            case "mod", "moderator" -> ROLE_MODERATOR;
            default -> throw new BadRequestException(
                    "role", "unknown value '%s'. Valid values: user, admin, mod, moderator".formatted(label));
        };
    }

    /**
     * Resolves a role from a filter query parameter (e.g. "ADMIN").
     * Used by JPA specifications — throws 400, not 404, on unknown values.
     */
    public static Role fromFilterString(String label) {
        if (label == null)
            throw new BadRequestException("role", "must not be null");
        try {
            return Role.valueOf("ROLE_" + label.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("role", "unknown filter value '%s'".formatted(label));
        }
    }
}