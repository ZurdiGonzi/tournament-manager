package com.tournament.app.domain.model;

import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;

import java.time.Instant;
import java.util.UUID;

public record User(
        UUID id,
        String username,
        String passwordHash,
        String role,
        Instant createdAt
) {
    public User {
        if (username == null || username.isBlank() || username.length() > 60) {
            throw new DomainValidationException("Username must be between 1 and 60 characters");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DomainValidationException("Password hash is required");
        }
        if (role == null || (!"PLAYER".equals(role) && !"ADMIN".equals(role))) {
            throw new DomainValidationException("Role must be PLAYER or ADMIN");
        }
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
