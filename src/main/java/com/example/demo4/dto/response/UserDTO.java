package com.example.demo4.dto.response;

import com.example.demo4.entity.Role;
import com.example.demo4.entity.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

/**
 * Full user projection — used for admin-facing detail views.
 */
public record UserDTO(
        Long id,
        String username,
        String email,
        String fullName,
        Set<Role> roles,
        Boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public static UserDTO fromEntity(User user) {
        if (user == null)
            return null;

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                // Defensive copy — prevents callers from mutating the entity's role set
                user.getRoles() != null ? Set.copyOf(user.getRoles()) : Collections.emptySet(),
                user.getEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}