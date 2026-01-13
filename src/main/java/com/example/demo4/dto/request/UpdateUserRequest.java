package com.example.demo4.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Partial update payload for a user — all fields are optional (PATCH
 * semantics).
 * A null field means "no change requested".
 */
public record UpdateUserRequest(
        @Email @Size(max = 100) String email,
        @Size(min = 6, max = 100) String password,
        @Size(max = 100) String fullName,
        Set<String> roles,
        Boolean enabled) {

    /** Returns an empty request — all fields mean "no change". */
    public static UpdateUserRequest empty() {
        return new UpdateUserRequest(null, null, null, null, null);
    }

    public UpdateUserRequest withEnabled(Boolean enabled) {
        return new UpdateUserRequest(email, password, fullName, roles, enabled);
    }

    public UpdateUserRequest withEmail(String email) {
        return new UpdateUserRequest(email, password, fullName, roles, enabled);
    }

    public UpdateUserRequest withPassword(String password) {
        return new UpdateUserRequest(email, password, fullName, roles, enabled);
    }

    public UpdateUserRequest withFullName(String fullName) {
        return new UpdateUserRequest(email, password, fullName, roles, enabled);
    }

    public UpdateUserRequest withRoles(Set<String> roles) {
        return new UpdateUserRequest(email, password, fullName, roles, enabled);
    }

    // Security override — exclude password from all log output

    @Override
    public String toString() {
        return "UpdateUserRequest[email=%s, password=****, fullName=%s, roles=%s, enabled=%s]"
                .formatted(email, fullName, roles, enabled);
    }
}