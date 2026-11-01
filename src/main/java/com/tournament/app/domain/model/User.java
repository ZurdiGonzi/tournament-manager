package com.tournament.app.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record User(
    UUID id,
    String username,
    String passwordHash,
    UserRole role,
    Instant createdAt
) {
    public User {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(username, "username must not be null");
        if (username.isBlank() || username.length() > 60) {
            throw new IllegalArgumentException("username must be between 1 and 60 characters");
        }
        Objects.requireNonNull(role, "role must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    /**
     * Returns a sanitized copy of the user with passwordHash omitted (`null`)
     * for public/profile read responses (FR-001, SC-004).
     */
    public User withoutPasswordHash() {
        return new User(id, username, null, role, createdAt);
    }
}
