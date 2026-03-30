package com.example.demo4.dto.response;

import com.example.demo4.entity.Role;
import com.example.demo4.entity.User;

import java.util.Collections;
import java.util.List;

/**
 * Slim user projection — used for paginated list views.
 * Roles are serialized as plain strings (without the "ROLE_" prefix stripped —
 * kept consistent with the entity enum name for frontend compatibility).
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        List<String> roles,
        Boolean enabled) {
    public static UserResponse fromEntity(User user) {
        if (user == null)
            return null;

        // Null-safe: getRoles() may return null for newly constructed users not yet
        // persisted
        List<String> roles = (user.getRoles() != null)
                ? user.getRoles().stream().map(Role::name).toList()
                : Collections.emptyList();

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                roles,
                user.getEnabled());
    }
}