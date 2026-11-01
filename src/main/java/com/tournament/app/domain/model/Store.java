package com.tournament.app.domain.model;

import com.tournament.app.domain.model.DomainExceptions.DomainValidationException;

import java.time.Instant;
import java.util.UUID;

public record Store(
        UUID id,
        String name,
        UUID ownerId,
        String currency,
        Instant createdAt
) {
    public Store {
        if (name == null || name.isBlank() || name.length() > 120) {
            throw new DomainValidationException("Store name must be between 1 and 120 characters");
        }
        if (ownerId == null) {
            throw new DomainValidationException("Store ownerId is required");
        }
        if (currency == null || (!"EUR".equals(currency) && !"USD".equals(currency))) {
            throw new DomainValidationException("Store currency must be EUR or USD");
        }
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean isOwnedBy(UUID userId) {
        return ownerId.equals(userId);
    }
}
